package win.doyto.query.core;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import win.doyto.query.annotation.NestedQueries;
import win.doyto.query.entity.Persistable;
import win.doyto.query.util.BeanUtil;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import static win.doyto.query.core.CommonUtil.*;
import static win.doyto.query.core.QuerySuffix.*;

/**
 * MemoryDataAccess
 *
 * @author f0rb
 */
@Slf4j
@SuppressWarnings({"unchecked", "java:S3740"})
public class MemoryDataAccess<E extends Persistable<I>, I extends Serializable, Q extends PageQuery> implements DataAccess<E, I, Q> {
    protected static final Map<Class<?>, Map<?, ?>> tableMap = new ConcurrentHashMap<>();

    protected final Map<I, E> entitiesMap = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);
    private final List<Field> fields;
    private final Field idField;
    private final Class<I> idFieldType;

    public MemoryDataAccess(Class<E> entityClass) {
        tableMap.put(entityClass, entitiesMap);

        // init fields
        Field[] allFields = FieldUtils.getAllFields(entityClass);
        List<Field> tempFields = new ArrayList<>(allFields.length);
        Arrays.stream(allFields).filter(CommonUtil::fieldFilter).forEachOrdered(tempFields::add);
        fields = Collections.unmodifiableList(tempFields);
        Field[] idFields = FieldUtils.getFieldsWithAnnotation(entityClass, Id.class);
        if (idFields.length == 1 && idFields[0].isAnnotationPresent(GeneratedValue.class)) {
            idField = idFields[0];
            Class<I> type;
            try {
                type = (Class<I>) BeanUtil.getActualTypeArguments(entityClass)[0];
            } catch (ClassCastException e) {
                type = (Class<I>) idField.getType();
            }
            idFieldType = type;
        } else {
            idField = null;
            idFieldType = null;
        }

    }

    protected void generateNewId(E entity) {
        try {
            Object newId = chooseIdValue(idGenerator.incrementAndGet(), idFieldType);
            writeField(idField, entity, newId);
        } catch (Exception e) {
            log.warn("写入id失败: {} - {}", entity.getClass(), e.getMessage());
        }
    }

    private Object chooseIdValue(Long newId, Class<?> type) {
        Object t = newId;
        if (type.isAssignableFrom(Integer.class)) {
            t = newId.intValue();
        }
        return t;
    }

    @Override
    public E get(IdWrapper<I> idWrapper) {
        return SerializationUtils.clone(entitiesMap.get(idWrapper.getId()));
    }

    @Override
    public List<I> queryIds(Q query) {
        return queryColumns(query, idFieldType, "id");
    }

    @Override
    public void create(E e) {
        if (idField != null) {
            generateNewId(e);
        }
        entitiesMap.put(e.getId(), e);
    }

    @Override
    public int update(E e) {
        return entitiesMap.put(e.getId(), e) == null ? 0 : 1;
    }

    @Override
    public int patch(E patch) {
        E origin = entitiesMap.get(patch.getId());
        if (origin == null) {
            return 0;
        }

        for (Field field : fields) {
            Object value = readField(field, patch);
            if (value != null) {
                writeField(field, origin, value);
            }
        }
        return 1;
    }

    @Override
    public int patch(E p, Q q) {
        List<E> list = query(q);
        for (E origin : list) {
            p.setId(origin.getId());
            patch(p);
        }
        return list.size();
    }

    @Override
    public int delete(IdWrapper<I> idWrapper) {
        return entitiesMap.remove(idWrapper.getId()) == null ? 0 : 1;
    }

    @Override
    public int delete(Q query) {
        List<E> list = query(query);
        list.stream().map(Persistable::getId).forEach(entitiesMap::remove);
        return list.size();
    }

    /**
     * 根据Query对象筛选符合条件的Entity对象
     *
     * @param query  Query
     * @param entity Entity
     * @return true, Entity符合条件需要保留; false, Entity不符合条件需要过滤掉
     */
    protected boolean filterByQuery(Q query, E entity) {
        for (Field field : query.getClass().getDeclaredFields()) {
            if (supportFilter(field)) {
                Object value = readField(field, query);
                if (isValidValue(value, field) && shouldDiscard(entity, field.getName(), value)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean supportFilter(Field field) {
        return fieldFilter(field) && !field.isAnnotationPresent(NestedQueries.class);
    }

    protected boolean shouldDiscard(E entity, String queryFieldName, Object queryFieldValue) {
        if (containsOr(queryFieldName)) {
            boolean result = true;
            for (String fieldName : splitByOr(queryFieldName)) {
                result &= shouldDiscard(entity, fieldName, queryFieldValue);
            }
            return result;
        }
        QuerySuffix querySuffix = resolve(queryFieldName);
        String columnName = querySuffix.resolveColumnName(queryFieldName);
        FilterExecutor.Matcher matcher = FilterExecutor.get(querySuffix);

        Object entityFieldValue = readField(entity, columnName);
        return !matcher.match(queryFieldValue, entityFieldValue);
    }

    @Override
    public List<E> query(Q query) {
        List<E> queryList = entitiesMap
                .values().stream()
                .filter(item -> filterByQuery(query, item))
                .collect(Collectors.toList());

        if (query.getSort() != null) {
            doSort(queryList, query.getSort());
        }
        if (query.needPaging()) {
            queryList = truncateByPaging(queryList, query);
        }

        return queryList;
    }

    private List<E> truncateByPaging(List<E> queryList, PageQuery pageQuery) {
        int from = pageQuery.calcOffset();
        int end = Math.min(queryList.size(), from + pageQuery.getPageSize());
        if (from <= end) {
            queryList = queryList.subList(from, end);
        }
        return queryList;
    }

    @Override
    @SneakyThrows
    public <V> List<V> queryColumns(Q q, Class<V> classV, String... columns) {
        List<E> entities = query(q);
        List<V> objects = new ArrayList<>(entities.size());
        if (columns.length == 1) {
            return entities.stream().map(entity -> (V) readField(entity, columns[0])).collect(Collectors.toList());
        } else {
            for (E e : entities) {
                objects.add(BeanUtil.convertTo(e, classV));
            }
        }
        return objects;
    }

    protected void doSort(List<E> queryList, String sort) {
        String[] orders = sort.split(";");
        for (int i = orders.length - 1; i >= 0; i--) {
            String order = orders[i];
            queryList.sort((o1, o2) -> {
                String[] pd = order.split(",");
                String property = toCamelCase(pd[0]);
                Comparable<Object> c1 = (Comparable<Object>) readField(o1, property);
                Object c2 = readField(o2, property);
                int ret = c1.compareTo(c2);
                return "asc".equalsIgnoreCase(pd[1]) ? ret : -ret;
            });
        }
    }

    @Override
    public long count(Q query) {
        return entitiesMap.values().stream().filter(item -> filterByQuery(query, item)).count();
    }

    private static class FilterExecutor {

        static final Map<QuerySuffix, Matcher> map = new EnumMap<>(QuerySuffix.class);

        static class LikeMatcher implements Matcher {
            @Override
            public boolean doMatch(Object qv, Object ev) {
                return StringUtils.contains(ev.toString(), qv.toString());
            }

            @Override
            public boolean isComparable(Object qv, Object ev) {
                return ev instanceof String;
            }
        }

        static class NotLikeMatcher extends LikeMatcher {
            @Override
            public boolean doMatch(Object qv, Object ev) {
                return !super.doMatch(qv, ev);
            }
        }

        static class StartMatcher extends LikeMatcher {
            @Override
            public boolean doMatch(Object qv, Object ev) {
                return StringUtils.startsWith(ev.toString(), qv.toString());
            }
        }

        static class NotNullMatcher implements Matcher {
            @Override
            public boolean doMatch(Object qv, Object ev) {
                return ev != null;
            }

            @Override
            public boolean isComparable(Object qv, Object ev) {
                return true;
            }
        }

        static class NullMatcher extends NotNullMatcher {
            @Override
            public boolean doMatch(Object qv, Object ev) {
                return !super.doMatch(qv, ev);
            }
        }

        static {
            map.put(Like, new LikeMatcher());
            map.put(NotLike, new NotLikeMatcher());
            map.put(Start, new StartMatcher());
            map.put(Null, new NullMatcher());
            map.put(NotNull, new NotNullMatcher());
            map.put(In, (qv, ev) -> ((Collection<?>) qv).contains(ev));
            map.put(NotIn, (qv, ev) -> !((Collection<?>) qv).contains(ev));
            map.put(Gt, (qv, ev) -> ((Comparable<Object>) ev).compareTo(qv) > 0);
            map.put(Lt, (qv, ev) -> ((Comparable<Object>) ev).compareTo(qv) < 0);
            map.put(Ge, (qv, ev) -> ((Comparable<Object>) ev).compareTo(qv) >= 0);
            map.put(Le, (qv, ev) -> ((Comparable<Object>) ev).compareTo(qv) <= 0);
            map.put(Not, (qv, ev) -> !qv.equals(ev));
        }

        static Matcher get(QuerySuffix querySuffix) {
            return map.getOrDefault(querySuffix, Object::equals);
        }

        interface Matcher {

            /**
             * 实体对象筛选
             *
             * @param qv 查询对象字段值
             * @param ev 实体对象字段值
             * @return true 符合过滤条件
             */
            boolean doMatch(Object qv, Object ev);

            default boolean match(Object qv, Object ev) {
                return isComparable(qv, ev) && doMatch(qv, ev);
            }

            default boolean isComparable(Object qv, Object ev) {
                return qv instanceof Collection || (qv instanceof Comparable && ev instanceof Comparable);
            }
        }

    }
}
