package win.doyto.query.user;

import win.doyto.query.service.AbstractCrudService;

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
