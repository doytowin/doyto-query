package win.doyto.query.demo.module.user;

import org.springframework.stereotype.Service;
import win.doyto.query.service.AbstractCrudService;

/**
 * UserDetailService
 *
 * @author f0rb on 2019-06-26
 */
@Service
public class UserDetailService extends AbstractCrudService<UserDetailEntity, Long, UserQuery> {

    @Override
    public boolean isNewEntity(UserDetailEntity userDetailEntity) {
        return fetch(userDetailEntity.getId()) == null;
    }
}
