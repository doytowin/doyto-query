package win.doyto.query.jpa2;

import win.doyto.query.core.AbstractService;
import win.doyto.query.core.QueryBuilder;
import win.doyto.query.core.QueryTable;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * AbstractJpa2Service
 *
 * @author f0rb
 * @date 2019-05-14
 */
public class AbstractJpa2Service<E, Q> extends AbstractService<E, Q> {
    QueryBuilder queryBuilder = new QueryBuilder();
    @Resource
    private EntityManager em;

    @SuppressWarnings("unchecked")
    @Override
    public List<E> query(Q q) {
        QueryTable queryTable = q.getClass().getAnnotation(QueryTable.class);
        List<Object> argList = new LinkedList<>();
        Query query = em.createNativeQuery(queryBuilder.buildSelectAndArgs(q, argList), queryTable.entityClass());
        Object[] args = argList.toArray();
        for (int i = 0; i < args.length; i++) {
            query.setParameter(i + 1, args[i]);
        }
        return query.getResultList();
    }

    @Override
    public long count(Q q) {
        List<Object> argList = new LinkedList<>();
        Query query = em.createNativeQuery(queryBuilder.buildCountAndArgs(q, argList));
        Object[] args = argList.toArray();
        for (int i = 0; i < args.length; i++) {
            query.setParameter(i + 1, args[i]);
        }
        return ((BigInteger) query.getSingleResult()).longValue();
    }
}
