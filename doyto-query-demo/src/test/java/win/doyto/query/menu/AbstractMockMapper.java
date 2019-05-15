package win.doyto.query.menu;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import win.doyto.query.core.DataAccess;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.Id;

/**
 * AbstractMockMapper
 *
 * @author f0rb
 * @date 2019-05-15
 */
@Slf4j
public abstract class AbstractMockMapper<E extends Persistable<I>, I extends Serializable, Q> implements DataAccess<E, I, Q> {

    protected static final Map<String, Map> tableMap = new ConcurrentHashMap<>();

    private final AtomicLong idGenerator = new AtomicLong(0);

    protected final Map<I, E> entitiesMap = new ConcurrentHashMap<>();

    public AbstractMockMapper(String table) {
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

    @Override
    public List<E> query(Q query) {
        return new ArrayList<>(entitiesMap.values());
    }

    @Override
    public long count(Q query) {
        return entitiesMap.size();
    }

}
