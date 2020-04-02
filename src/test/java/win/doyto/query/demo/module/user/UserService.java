package win.doyto.query.demo.module.user;

import org.springframework.stereotype.Service;
import win.doyto.query.service.AbstractCrudService;

/**
 * UserService
 *
 * @author f0rb on 2020-04-02
 */
@Service
public class UserService extends AbstractCrudService<UserEntity, Long, UserQuery> {
}
