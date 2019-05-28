package win.doyto.query.module.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import win.doyto.query.exception.ServiceException;
import win.doyto.query.service.AbstractRestService;

import java.util.Objects;

/**
 * UserController
 *
 * @author f0rb
 */
@Slf4j
@RestController
@RequestMapping("user")
class UserController extends AbstractRestService<UserEntity, Long, UserQuery, UserRequest, UserResponse> implements UserService {

    @Override
    protected String getCacheName() {
        return "module:user";
    }

    @Override
    public UserResponse auth(String account, String password) {
        UserEntity userEntity = get(UserQuery.builder().usernameOrEmailOrMobile(account).build());
        if (userEntity == null) {
            throw new ServiceException("账号不存在");
        }
        if (!userEntity.isValid()) {
            throw new ServiceException("账号被禁用");
        }

        if (!Objects.equals(userEntity.getPassword(), password)) {
            throw new ServiceException("密码错误");
        }
        return getEntityView().from(userEntity);
    }
}
