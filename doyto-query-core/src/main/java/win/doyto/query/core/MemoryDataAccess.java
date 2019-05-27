package win.doyto.query.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import javax.persistence.Id;

import static win.doyto.query.core.QueryBuilder.ignoreField;
import static win.doyto.query.core.QueryBuilder.readField;
import static win.doyto.query.core.QuerySuffix.*;

/**
 * MemoryDataAccess
 *
 * @author f0rb
 */
@Slf4j
@SuppressWarnings({"unchecked", "squid:S1135"})
public class MemoryDataAccess<E extends Persistable<I>, I extends Serializable, Q> implements DataAccess<E, I, Q> {
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
            if (!ignoreField(field)) {
                Object v1 = readField(field, query);
                if (v1 != null) {
                    boolean shouldNotRemain = unsatisfied(entity, field.getName(), v1);
                    if (shouldNotRemain) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    protected Boolean unsatisfied(E entity, String queryFieldName, Object queryFieldValue) {
        QuerySuffix querySuffix = resolve(queryFieldName);
        String columnName = querySuffix.resolveColumnName(queryFieldName);
        FilterExecutor filterExecutor = filterExecutorMap.get(querySuffix);

        if (columnName.contains("Or")) {
            String[] names = ColumnMeta.splitByOr(columnName);
            return Arrays.stream(names).
                map(name -> readField(entity, ColumnMeta.camelize(name))).
                noneMatch(entityFieldValue -> filterExecutor.match(queryFieldValue, entityFieldValue));
        } else {
            Object entityFieldValue = readField(entity, columnName);
            return !filterExecutor.match(queryFieldValue, entityFieldValue);
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

    @SuppressWarnings("unused")
    protected void doSort(List<E> queryList, String sort) {
        // TODO support later
    }

    @Override
    public long count(Q query) {
        return entitiesMap.values().stream().filter(item -> filterByQuery(query, item)).count();
    }

    public void reset() {
        entitiesMap.clear();
        idGenerator.set(0);
    }

    interface FilterExecutor {
        /**
         * 实体对象筛选
         *
         * @param queryFieldValue  查询对象字段值
         * @param entityFieldValue 实体对象字段值
         * @return true 符合过滤条件
         */
        boolean match(Object queryFieldValue, Object entityFieldValue);
    }

    static final Map<QuerySuffix, FilterExecutor> filterExecutorMap = new EnumMap<>(QuerySuffix.class);

    static {
        filterExecutorMap.put(Like, (queryFieldValue, entityFieldValue) ->
            entityFieldValue instanceof String && StringUtils.contains(((String) entityFieldValue), (String) queryFieldValue));

        filterExecutorMap.put(In, (queryFieldValue, entityFieldValue) ->
            queryFieldValue instanceof Collection && ((Collection) queryFieldValue).contains(entityFieldValue));

        filterExecutorMap.put(NotIn, (queryFieldValue, entityFieldValue) ->
            queryFieldValue instanceof Collection && !((Collection) queryFieldValue).contains(entityFieldValue));

        filterExecutorMap.put(Gt, (queryFieldValue, entityFieldValue) ->
            !(queryFieldValue instanceof Comparable) || !(entityFieldValue instanceof Comparable) ||
                ((Comparable) entityFieldValue).compareTo(queryFieldValue) > 0);

        filterExecutorMap.put(Lt, (queryFieldValue, entityFieldValue) ->
            !(queryFieldValue instanceof Comparable) || !(entityFieldValue instanceof Comparable) ||
                ((Comparable) entityFieldValue).compareTo(queryFieldValue) < 0);

        filterExecutorMap.put(Ge, (queryFieldValue, entityFieldValue) ->
            !(queryFieldValue instanceof Comparable) || !(entityFieldValue instanceof Comparable) ||
                ((Comparable) entityFieldValue).compareTo(queryFieldValue) >= 0);

        filterExecutorMap.put(Le, (queryFieldValue, entityFieldValue) ->
            !(queryFieldValue instanceof Comparable) || !(entityFieldValue instanceof Comparable) ||
                ((Comparable) entityFieldValue).compareTo(queryFieldValue) <= 0);

        filterExecutorMap.put(NONE, Object::equals);
    }
}
