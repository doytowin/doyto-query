package win.doyto.query.service;

import org.springframework.beans.BeanUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.ReflectionUtils;
import win.doyto.query.core.PageQuery;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AbstractDynamicService
 *
 * @author f0rb on 2019-05-26
 */
@SuppressWarnings("squid:S00112")
public abstract class AbstractDynamicService<E extends Persistable<I>, I extends Serializable, Q extends PageQuery>
    extends AbstractService<E, I, Q> implements DynamicService<E, I, Q> {

    private Constructor<E> constructor;

    public AbstractDynamicService() {
        try {
            constructor = ReflectionUtils.accessibleConstructor(entityType);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public final E get(E param) {
        return entityCacheWrapper.execute(resolveCacheKey(param), () -> fetch(param));
    }

    @Override
    public final E fetch(E param) {
        return dataAccess.get(param);
    }

    @Override
    public final E delete(E param) {
        E e = get(param);
        if (e != null) {
            if (!entityAspects.isEmpty()) {
                transactionOperations.execute(s -> {
                    dataAccess.delete(param);
                    entityAspects.forEach(entityAspect -> entityAspect.afterDelete(e));
                    return null;
                });
            } else {
                dataAccess.delete(param);
            }
            String key = resolveCacheKey(e);
            entityCacheWrapper.evict(key);
            entityCacheWrapper.execute(key, () -> null);
        }
        return e;
    }

    /**
     * NOTICE: You'd better to overwrite this method to make it faster
     */
    @Override
    public List<E> query(Q query) {
        if (caching() && !TransactionSynchronizationManager.isActualTransactionActive()) {
            return queryIds(query).stream().map(id -> {
                try {
                    E e = constructor.newInstance();
                    BeanUtils.copyProperties(query, e);
                    e.setId(id);
                    return get(e);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }).collect(Collectors.toList());
        }
        return dataAccess.query(query);
    }
}
