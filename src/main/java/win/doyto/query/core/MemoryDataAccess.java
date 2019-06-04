package win.doyto.query.core;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import javax.persistence.Id;

import static win.doyto.query.core.CommonUtil.*;
import static win.doyto.query.core.QuerySuffix.*;

/**
 * MemoryDataAccess
 *
 * @author f0rb
 */
@Slf4j
@SuppressWarnings({"unchecked", "squid:S1135"})
class MemoryDataAccess<E extends Persistable<I>, I extends Serializable, Q> implements DataAccess<E, I, Q> {
    protected static final Map<Object, Map> tableMap = new ConcurrentHashMap<>();

    protected final Map<I, E> entitiesMap = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public MemoryDataAccess(Object table) {
        tableMap.put(table, entitiesMap);
    }

    protected void generateNewId(E entity) {
        Field[] fields = FieldUtils.getAllFields(entity.getClass());
        for (Field field : fields) {
            if (field.isAnnotationPresent(Id.class)) {
                try {
                    Object newId = chooseIdValue(idGenerator.incrementAndGet(), field.getType());
                    FieldUtils.writeField(field, entity, newId, true);
                } catch (Exception e) {
                    log.warn("写入id失败: {} - {}", entity.getClass(), e.getMessage());
                }
                break;
            }
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
    public E get(I id) {
        return entitiesMap.get(id);
    }

    @Override
    public E fetch(I id) {
        E e = entitiesMap.get(id);
        return SerializationUtils.clone(e);
    }

    @Override
    public void create(E e) {
        generateNewId(e);
        entitiesMap.put(e.getId(), e);
    }

    @Override
    public void update(E e) {
        entitiesMap.put(e.getId(), e);
    }

    @Override
    public void patch(E e) {
        // TODO patch
        update(e);
    }

    @Override
    public int patch(E e, Q q) {
        List<E> list = query(q);
        list.forEach(this::patch);
        return list.size();
    }

    @Override
    public int delete(I id) {
        return entitiesMap.remove(id) == null ? 0 : 1;
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
            if (!ignoreField(field) && supportFilter(field)) {
                Object v1 = readField(field, query);
                if (isValidValue(v1, field)) {
                    boolean shouldNotRemain = unsatisfied(entity, field.getName(), v1);
                    if (shouldNotRemain) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean supportFilter(Field field) {
        return !field.isAnnotationPresent(SubQuery.class) && !field.isAnnotationPresent(NestedQueries.class);
    }

    protected Boolean unsatisfied(E entity, String queryFieldName, Object queryFieldValue) {
        QuerySuffix querySuffix = resolve(queryFieldName);
        String columnName = querySuffix.resolveColumnName(queryFieldName);
        FilterExecutor.Matcher matcher = FilterExecutor.get(querySuffix);

        if (columnName.contains("Or")) {
            String[] names = splitByOr(columnName);
            return Arrays.stream(names).
                map(name -> readField(entity, camelize(name))).
                noneMatch(entityFieldValue -> matcher.match(queryFieldValue, entityFieldValue));
        } else {
            Object entityFieldValue = readField(entity, columnName);
            return !matcher.match(queryFieldValue, entityFieldValue);
        }
    }

    @Override
    public List<E> query(Q query) {
        List<E> queryList = entitiesMap
            .values().stream()
            .filter(item -> filterByQuery(query, item))
            .collect(Collectors.toList());

        if (query instanceof PageQuery) {
            PageQuery pageQuery = (PageQuery) query;
            if (pageQuery.getSort() != null) {
                doSort(queryList, pageQuery.getSort());
            }
            if (pageQuery.needPaging()) {
                int from = pageQuery.getPageNumber() * pageQuery.getPageSize();
                int end = Math.min(queryList.size(), from + pageQuery.getPageSize());
                if (from <= end) {
                    queryList = new ArrayList<>(queryList.subList(from, end));
                }
            }
        }

        return queryList;
    }

    @Override
    @SneakyThrows
    public <V> List<V> queryColumns(Q q, RowMapper<V> rowMapper, String... columns) {
        List<E> entities = query(q);
        List<V> objects = new ArrayList<>(entities.size());
        if (rowMapper instanceof SingleColumnRowMapper) {
            return entities.stream().map(entity -> (V) readField(entity, columns[0])).collect(Collectors.toList());
        } else {
            Class<V> classV = (Class<V>) readField(rowMapper, "mappedClass");
            V v = classV.getDeclaredConstructor().newInstance();
            entities.forEach(e -> {
                BeanUtils.copyProperties(e, v);
                objects.add(v);
            });
        }
        return objects;
    }

    @SuppressWarnings("unused")
    protected void doSort(List<E> queryList, String sort) {
        // TODO support later
    }

    @Override
    public long count(Q query) {
        return entitiesMap.values().stream().filter(item -> filterByQuery(query, item)).count();
    }

    private static class FilterExecutor {

        static final Map<QuerySuffix, Matcher> map = new EnumMap<>(QuerySuffix.class);

        static class NotLikeMatcher implements Matcher {
            @Override
            public boolean doMatch(Object qv, Object ev) {
                return !StringUtils.contains(((String) ev), (String) qv);
            }

            @Override
            public boolean isComparable(Object qv, Object ev) {
                return ev instanceof String;
            }
        }

        static class LikeMatcher extends NotLikeMatcher {
            @Override
            public boolean doMatch(Object qv, Object ev) {
                return !super.doMatch(qv, ev);
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
            map.put(Null, new NullMatcher());
            map.put(NotNull, new NotNullMatcher());
            map.put(In, (qv, ev) -> ((Collection) qv).contains(ev));
            map.put(NotIn, (qv, ev) -> !((Collection) qv).contains(ev));
            map.put(Gt, (qv, ev) -> ((Comparable) ev).compareTo(qv) > 0);
            map.put(Lt, (qv, ev) -> ((Comparable) ev).compareTo(qv) < 0);
            map.put(Ge, (qv, ev) -> ((Comparable) ev).compareTo(qv) >= 0);
            map.put(Le, (qv, ev) -> ((Comparable) ev).compareTo(qv) <= 0);
            map.put(NONE, Object::equals);
        }

        static Matcher get(QuerySuffix querySuffix) {
            return map.get(querySuffix);
        }

        interface Matcher {

            /**
             * 实体对象筛选
             *
             * @param qv  查询对象字段值
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
