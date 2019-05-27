package win.doyto.query.user;

import win.doyto.query.core.AbstractCrudService;

/**
 * UserService
 *
 * @author f0rb
 */
public class UserService extends AbstractCrudService<UserEntity, Integer, UserQuery> {
    @Override
    protected String getCacheName() {
        return "module:user";
    }
}
