package win.doyto.query.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import win.doyto.query.core.PageList;
import win.doyto.query.core.QueryBuilder;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * UserService
 *
 * @author f0rb
 * @date 2019-05-12
 */
@Slf4j
@Service
class UserService {

    @Resource
    private EntityManager em;

    @Resource
    private UserRepository userRepository;

    QueryBuilder queryBuilder = new QueryBuilder();

    @SuppressWarnings("unchecked")
    public List<UserEntity> query(UserQuery userQuery) {
        List<Object> argList = new LinkedList<>();
        Query query = em.createNativeQuery(queryBuilder.buildSelectAndArgs(userQuery, argList), UserEntity.class);
        Object[] args = argList.toArray();
        for (int i = 0; i < args.length; i++) {
            query.setParameter(i + 1, args[i]);
        }
        return query.getResultList();
    }

    public long count(UserQuery userQuery) {
        List<Object> argList = new LinkedList<>();
        Query query = em.createNativeQuery(queryBuilder.buildCountAndArgs(userQuery, argList));
        Object[] args = argList.toArray();
        for (int i = 0; i < args.length; i++) {
            query.setParameter(i + 1, args[i]);
        }
        return ((BigInteger) query.getSingleResult()).longValue();
    }

    public PageList<UserEntity> page(UserQuery userQuery) {
        return new PageList<>(query(userQuery), count(userQuery));
    }
}
