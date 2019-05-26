package win.doyto.query.module.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import win.doyto.query.core.AbstractCrudService;

/**
 * UserService
 *
 * @author f0rb
 */
@Slf4j
@Service
class UserService extends AbstractCrudService<UserEntity, Long, UserQuery> {
    @Override
    protected String getCacheName() {
        return "module:user";
    }
}
