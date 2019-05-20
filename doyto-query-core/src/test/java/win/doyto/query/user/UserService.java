package win.doyto.query.user;

import win.doyto.query.core.AbstractCrudService;
import win.doyto.query.core.DataAccess;

/**
 * UserService
 *
 * @author f0rb
 */
public class UserService extends AbstractCrudService<UserEntity, Integer, UserQuery> {
    public UserService(DataAccess<UserEntity, Integer, UserQuery> dataAccess) {
        super(dataAccess);
    }

    @Override
    protected String getCacheName() {
        return "module:user";
    }
}
