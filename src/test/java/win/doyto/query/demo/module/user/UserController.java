package win.doyto.query.demo.module.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import win.doyto.query.demo.exception.ServiceAsserts;
import win.doyto.query.service.AbstractRestController;
import win.doyto.query.validation.CreateGroup;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Resource;

/**
 * UserController
 *
 * @author f0rb
 */
@Slf4j
@RestController
@RequestMapping("user")
class UserController extends AbstractRestController<UserEntity, Long, UserQuery, UserRequest, UserResponse> implements UserService {

    @Resource
    UserDetailService userDetailService;

    @Override
    protected String getCacheName() {
        return "module:user";
    }

    @Override
    @GetMapping("{id}")
    public UserResponse getById(@PathVariable Long id) {
        UserResponse userResponse = super.getById(id);
        UserDetailEntity userDetailEntity = userDetailService.get(id);
        if (userDetailEntity != null) {
            userResponse.setAddress(userDetailEntity.getAddress());
        }
        return userResponse;
    }

    @Override
    @PostMapping
    public void create(@RequestBody @Validated(CreateGroup.class) UserRequest request) {
        UserEntity userEntity = save(request.toEntity());
        userDetailService.save(UserDetailEntity.build(userEntity.getId(), request));
    }

    @Override
    protected RowMapper<UserEntity> getRowMapper() {
        return (rs, rn) -> {
            UserEntity userEntity = new UserEntity();
            userEntity.setId(rs.getLong("id"));
            userEntity.setUsername(rs.getString("username"));
            userEntity.setPassword(rs.getString("password"));
            userEntity.setNickname(rs.getString("nickname"));
            userEntity.setMobile(rs.getString("mobile"));
            userEntity.setEmail(rs.getString("email"));
            userEntity.setMemo(rs.getString("memo"));
            userEntity.setUserLevel(UserLevel.valueOf(rs.getString("userLevel")));
            userEntity.setValid(rs.getBoolean("valid"));
            return userEntity;
        };
    }

    @Override
    public List<UserResponse> list(UserQuery q) {
        return queryColumns(q, UserResponse.class, "id", "username", "mobile", "email", "nickname", "valid", "userLevel", "memo");
    }

    @GetMapping("column/{column:\\w+}")
    public List<String> listColumn(UserQuery q, @PathVariable String column) {
        return queryColumns(q, String.class, column);
    }

    @GetMapping("columns/{columns:[\\w,]+}")
    public List<Map> listColumns(UserQuery q, @PathVariable String columns) {
        return queryColumns(q, Map.class, columns);
    }

    @Override
    public UserResponse auth(String account, String password) {
        UserEntity userEntity = get(UserQuery.builder().usernameOrEmailOrMobile(account).build());
        ServiceAsserts.notNull(userEntity, "账号不存在");
        ServiceAsserts.isTrue(userEntity.getValid(), "账号被禁用");
        ServiceAsserts.isTrue(Objects.equals(userEntity.getPassword(), password), "密码错误");
        return getEntityView().buildBy(userEntity);
    }

    @PostMapping("memo")
    public void updateMemo(@RequestBody UserRequest request) {
        UserEntity userEntity = new UserEntity();
        userEntity.setMemo(request.getMemo());
        UserQuery byEmail = UserQuery.builder().emailLike(request.getEmail()).memoNull(true).build();
        patch(userEntity, byEmail);
    }

}
