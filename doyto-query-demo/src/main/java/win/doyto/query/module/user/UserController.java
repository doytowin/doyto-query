package win.doyto.query.module.user;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import win.doyto.query.core.PageList;
import win.doyto.query.exception.ServiceException;

import java.util.List;
import java.util.Objects;

/**
 * UserController
 *
 * @author f0rb
 */
@Slf4j
@RestController
@RequestMapping("user")
@AllArgsConstructor
@SuppressWarnings("squid:S4529")
public class UserController implements UserApi {

    UserService userService;

    @GetMapping("query")
    public List<UserResponse> query(UserQuery userQuery) {
        return userService.query(userQuery, UserResponse::of);
    }

    @GetMapping("page")
    public PageList<UserResponse> page(UserQuery userQuery) {
        return userService.page(userQuery, UserResponse::of);
    }

    @GetMapping("get")
    public UserResponse get(Long id) {
        UserEntity userEntity = userService.get(id);
        if (userEntity == null) {
            throw new ServiceException("账号不存在");
        }
        return UserResponse.of(userEntity);
    }

    @PostMapping("save")
    public void save(@RequestBody UserRequest userRequest) {
        userService.save(userRequest.toEntity());
    }

    @PostMapping("delete")
    public void delete(Long id) {
        UserEntity userEntity = userService.delete(id);
        if (userEntity == null) {
            throw new ServiceException("User not found");
        }
    }

    @Override
    public UserResponse auth(String account, String password) {
        UserEntity userEntity = userService.get(UserQuery.builder().usernameOrEmailOrMobile(account).build());
        if (userEntity == null) {
            throw new ServiceException("账号不存在");
        }
        if (!userEntity.isValid()) {
            throw new ServiceException("账号被禁用");
        }

        if (!Objects.equals(userEntity.getPassword(), password)) {
            throw new ServiceException("密码错误");
        }
        return UserResponse.of(userEntity);
    }
}
