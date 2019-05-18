package win.doyto.query.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * UserIdProvider
 *
 * @author f0rb
 * @date 2019-05-17
 */
public interface UserIdProvider<I extends Serializable> {

    I getUserId();

    @SuppressWarnings("unchecked")
    default void setupUserId(Object e) {
        I userId = getUserId();
        if (userId != null) {
            if (e instanceof Persistable && ((Persistable) e).isNew()) {
                if( e instanceof CreateUserAware) {
                    ((CreateUserAware<I>) e).setCreateUserId(userId);
                    ((CreateUserAware) e).setCreateTime(new Date());
                }
            } else {
                if (e instanceof UpdateUserAware) {
                    ((UpdateUserAware<I>) e).setUpdateUserId(userId);
                    ((UpdateUserAware) e).setUpdateTime(new Date());
                }
            }
        }
    }

}