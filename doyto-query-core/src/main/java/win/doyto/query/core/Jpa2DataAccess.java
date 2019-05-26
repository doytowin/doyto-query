package win.doyto.query.core;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * Jpa2DataAccess
 *
 * @author f0rb
 */
@SuppressWarnings("squid:S00119")
class Jpa2DataAccess<E, ID extends Serializable, Q> implements DataAccess<E, ID, Q> {

    QueryBuilder queryBuilder = QueryBuilder.instance();

    private EntityManager em;

    private Class<E> domainType;

    public Jpa2DataAccess(Class<E> domainType, EntityManager em) {
        this.domainType = domainType;
        this.em = em;
    }

    @SuppressWarnings("unchecked")
    public List<E> query(Q q) {
        List<Object> argList = new LinkedList<>();
        Query query = em.createNativeQuery(queryBuilder.buildSelectAndArgs(q, argList), domainType);
        Object[] args = argList.toArray();
        for (int i = 0; i < args.length; i++) {
            query.setParameter(i + 1, args[i]);
        }
        return query.getResultList();
    }

    public long count(Q q) {
        List<Object> argList = new LinkedList<>();
        Query query = em.createNativeQuery(queryBuilder.buildCountAndArgs(q, argList));
        Object[] args = argList.toArray();
        for (int i = 0; i < args.length; i++) {
            query.setParameter(i + 1, args[i]);
        }
        return ((BigInteger) query.getSingleResult()).longValue();
    }

    @Override
    public E get(ID id) {
        Assert.notNull(id, "The given id must not be null!");
        return this.em.find(domainType, id);
    }

    @Override
    @Transactional
    public int delete(ID id) {
        Assert.notNull(id, "The given id must not be null!");
        E entity = this.get(id);
        if (entity == null) {
            return 0;
        } else {
            this.delete(entity);
            return 1;
        }
    }

    @Transactional
    public void delete(E entity) {
        Assert.notNull(entity, "The entity must not be null!");
        this.em.remove(this.em.contains(entity) ? entity : this.em.merge(entity));
    }

    @Override
    public void create(E e) {
        this.em.persist(e);
    }

    @Override
    public void update(E e) {
        this.em.merge(e);
    }

    @Override
    public E fetch(ID id) {
        E e = get(id);
        if (e == null) {
            return null;
        }
        this.em.detach(e);
        return get(id);
    }

}
