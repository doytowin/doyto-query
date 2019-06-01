package win.doyto.query.demo.module.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import win.doyto.query.demo.exception.ServiceAsserts;
import win.doyto.query.service.AbstractRestService;

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
class UserController extends AbstractRestService<UserEntity, Long, UserQuery, UserRequest, UserResponse> implements UserService {

    private BeanPropertyRowMapper<UserResponse> userResponseRowMapper = new BeanPropertyRowMapper<>(UserResponse.class);

    @Override
    protected String getCacheName() {
        return "module:user";
    }

    @Override
    public List<UserResponse> list(UserQuery q) {
        return queryColumns(q, UserResponse.class,
            "id", "username", "mobile", "email", "nickname", "valid", "userLevel", "memo");
    }

    @Override
    public UserResponse auth(String account, String password) {
        UserEntity userEntity = get(UserQuery.builder().usernameOrEmailOrMobile(account).build());
        ServiceAsserts.notNull(userEntity, "账号不存在");
        ServiceAsserts.isTrue(userEntity.getValid(), "账号被禁用");
        ServiceAsserts.isTrue(Objects.equals(userEntity.getPassword(), password), "密码错误");
        return getEntityView().from(userEntity);
    }

    @PostMapping("memo")
    public void updateMemo(@RequestBody UserRequest request) {
        UserEntity userEntity = new UserEntity();
        userEntity.setMemo(request.getMemo());
        UserQuery byEmail = UserQuery.builder().emailLike(request.getEmail()).memoNull(true).build();
        patch(userEntity, byEmail);
    }

}
