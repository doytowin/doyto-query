package win.doyto.query.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import javax.persistence.Id;

import static win.doyto.query.core.QueryBuilder.ignoreField;
import static win.doyto.query.core.QueryBuilder.readField;

/**
 * AbstractMockDataAccess
 *
 * @author f0rb
 * @date 2019-05-15
 */
@Slf4j
public abstract class AbstractMockDataAccess<E extends Persistable<I>, I extends Serializable, Q> implements DataAccess<E, I, Q> {
    protected static final Map<String, Map> tableMap = new ConcurrentHashMap<>();
    protected final Map<I, E> entitiesMap = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public AbstractMockDataAccess(String table) {
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
    public void create(E e) {
        generateNewId(e);
        entitiesMap.put(e.getId(), e);
    }

    @Override
    public void update(E e) {
        entitiesMap.put(e.getId(), e);
    }

    @Override
    public void delete(I id) {
        entitiesMap.remove(id);
    }

    protected boolean filterByQuery(Q query, E entity) {
        for (Field field : query.getClass().getDeclaredFields()) {
            if (!ignoreField(field)) {
                Object value = readField(field, query);
                if (value != null) {
                    Object v2 = null;
                    try {
                        v2 = FieldUtils.readField(entity, field.getName(), true);
                    } catch (IllegalAccessException e) {
                        log.error("FieldUtils.readField failed: {}", e.getMessage());
                    }
                    if (!value.equals(v2)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public List<E> query(Q query) {
        List<E> queryList = entitiesMap
            .values().stream()
            .filter(item -> filterByQuery(query, item))
            .collect(Collectors.toList());

        if (query instanceof PageQuery) {
            PageQuery pageQuery = (PageQuery) query;
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
    public long count(Q query) {
        return entitiesMap.size();
    }
}
