package win.doyto.query.module.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import win.doyto.query.jpa2.AbstractJpa2Service;

/**
 * UserService
 *
 * @author f0rb
 */
@Slf4j
@Service
class UserService extends AbstractJpa2Service<UserEntity, Long, UserQuery> {

    public UserService(UserRepository userRepository) {
        super(userRepository);
    }

    @Override
    protected String getCacheName() {
        return "module:user";
    }
}
