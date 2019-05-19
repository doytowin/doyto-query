package win.doyto.query.jpa2;

import org.springframework.data.repository.CrudRepository;
import win.doyto.query.core.DataAccess;
import win.doyto.query.core.QueryBuilder;
import win.doyto.query.core.QueryService;
import win.doyto.query.core.QueryTable;

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
 * @date 2019-05-14
 */
public class Jpa2DataAccess<E, I extends Serializable, Q> implements DataAccess<E, I, Q> {

    QueryBuilder queryBuilder = QueryBuilder.instance();

    CrudRepository<E, I> crudRepository;

    private EntityManager em;

    public Jpa2DataAccess(CrudRepository<E, I> crudRepository) {
        this.crudRepository = crudRepository;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.em = entityManager;
    }

    @SuppressWarnings("unchecked")
    public List<E> query(Q q) {
        if (crudRepository instanceof QueryService) {
            return ((QueryService) crudRepository).query(q);
        }
        QueryTable queryTable = q.getClass().getAnnotation(QueryTable.class);
        List<Object> argList = new LinkedList<>();
        Query query = em.createNativeQuery(queryBuilder.buildSelectAndArgs(q, argList), queryTable.entityClass());
        Object[] args = argList.toArray();
        for (int i = 0; i < args.length; i++) {
            query.setParameter(i + 1, args[i]);
        }
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public long count(Q q) {
        if (crudRepository instanceof QueryService) {
            return ((QueryService) crudRepository).count(q);
        }
        List<Object> argList = new LinkedList<>();
        Query query = em.createNativeQuery(queryBuilder.buildCountAndArgs(q, argList));
        Object[] args = argList.toArray();
        for (int i = 0; i < args.length; i++) {
            query.setParameter(i + 1, args[i]);
        }
        return ((BigInteger) query.getSingleResult()).longValue();
    }

    @Override
    public E get(I id) {
        return crudRepository.findById(id).orElse(null);
    }

    public E fetch(I id) {
        crudRepository.findById(id).ifPresent(em::detach);
        return get(id);
    }

    @Override
    public void delete(I id) {
        crudRepository.deleteById(id);
    }

    @Override
    public void create(E e) {
        crudRepository.save(e);
    }

    @Override
    public void update(E e) {
        crudRepository.save(e);
    }

}
