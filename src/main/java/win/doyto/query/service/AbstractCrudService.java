package win.doyto.query.service;

import org.springframework.transaction.support.TransactionSynchronizationManager;
import win.doyto.query.core.PageQuery;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AbstractCrudService
 *
 * @author f0rb
 */
public abstract class AbstractCrudService<E extends Persistable<I>, I extends Serializable, Q extends PageQuery>
    extends AbstractService<E, I, Q> implements CrudService<E, I, Q> {

    @Override
    protected final String resolveCacheKey(E e) {
        return String.valueOf(e.getId());
    }

    @Override
    public final E get(I id) {
        return entityCacheWrapper.execute(id.toString(), () -> fetch(id));
    }

    @Override
    public final E fetch(I id) {
        return dataAccess.get(id);
    }

    @Override
    public final E delete(I id) {
        E e = get(id);
        if (e != null) {
            if (!entityAspects.isEmpty()) {
                transactionOperations.execute(s -> {
                    dataAccess.delete(id);
                    entityAspects.forEach(entityAspect -> entityAspect.afterDelete(e));
                    return null;
                });
            } else {
                dataAccess.delete(id);
            }
            String key = id.toString();
            entityCacheWrapper.evict(key);
            entityCacheWrapper.execute(key, () -> null);
        }
        return e;
    }

    @Override
    public final List<E> query(Q query) {
        if (caching() && !TransactionSynchronizationManager.isActualTransactionActive()) {
            return queryIds(query).stream().map(dataAccess::get).collect(Collectors.toList());
        }
        return dataAccess.query(query);
    }

}
