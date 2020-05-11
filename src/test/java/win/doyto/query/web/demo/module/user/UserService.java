package win.doyto.query.web.demo.module.user;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import win.doyto.query.service.AbstractCrudService;

/**
 * UserService
 *
 * @author f0rb on 2020-04-02
 */
@Primary
@Service
public class UserService extends AbstractCrudService<UserEntity, Long, UserQuery> {
}
