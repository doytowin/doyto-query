package win.doyto.query.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import win.doyto.query.core.PageQuery;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.Id;

/**
 * ReactiveEIQController
 *
 * @author f0rb on 2021-10-26
 */
@Slf4j
public abstract class ReactiveEIQController <E extends Persistable<I>, I extends Serializable, Q extends PageQuery> {

    Map<I, E> entitiesMap = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public Mono<E> add(E e) {
        generateNewId(e);
        entitiesMap.put(e.getId(), e);
        return Mono.just(e);
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

    @SuppressWarnings("java:S1172")
    public Flux<E> query(Q query) {
        List<E> queryList = new ArrayList<>(entitiesMap.values());
        return Flux.fromIterable(queryList);
    }
}
