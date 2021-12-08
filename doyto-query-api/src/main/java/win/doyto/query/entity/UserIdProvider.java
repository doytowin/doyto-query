package win.doyto.query.entity;

import java.io.Serializable;

/**
 * UserIdProvider
 *
 * @author f0rb
 */
public interface UserIdProvider<I extends Serializable> {

    I getUserId();

    @SuppressWarnings("unchecked")
    default void setupUserId(Object e) {
        I userId = getUserId();
        if (userId != null) {
            if (e instanceof Persistable && ((Persistable<?>) e).isNew() && e instanceof CreateUserAware) {
                CreateUserAware<I> createUserAware = (CreateUserAware<I>) e;
                createUserAware.setCreateUserId(userId);
            }
            if (e instanceof UpdateUserAware) {
                UpdateUserAware<I> updateUserAware = (UpdateUserAware<I>) e;
                updateUserAware.setUpdateUserId(userId);
            }
        }
    }

}