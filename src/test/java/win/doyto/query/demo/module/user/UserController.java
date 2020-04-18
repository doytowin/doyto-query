package win.doyto.query.demo.module.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import win.doyto.query.demo.exception.ServiceAsserts;
import win.doyto.query.validation.CreateGroup;
import win.doyto.query.web.controller.AbstractRestController;

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
class UserController extends AbstractRestController<UserEntity, Long, UserQuery, UserRequest, UserResponse> implements UserApi {

    @Resource
    UserService userService;

    @Resource
    UserDetailService userDetailService;

    @Override
    protected UserService getService() {
        return userService;
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
    public void add(@RequestBody @Validated(CreateGroup.class) UserRequest request) {
        UserEntity userEntity = userService.save(buildEntity(request));
        userDetailService.save(UserDetailEntity.build(userEntity.getId(), request));
    }

    @Override
    public List<UserResponse> list(UserQuery q) {
        return userService.queryColumns(q, UserResponse.class, "id", "username", "mobile", "email", "nickname", "valid", "userLevel", "memo");
    }

    @GetMapping("column/{column:\\w+}")
    public List<String> listColumn(UserQuery q, @PathVariable String column) {
        return userService.queryColumns(q, String.class, column);
    }

    @GetMapping("columns/{columns:[\\w,]+}")
    public List<Map> listColumns(UserQuery q, @PathVariable String columns) {
        return userService.queryColumns(q, Map.class, columns);
    }

    @Override
    public UserResponse auth(String account, String password) {
        UserEntity userEntity = userService.get(UserQuery.builder().usernameOrEmailOrMobile(account).build());
        ServiceAsserts.notNull(userEntity, "账号不存在");
        ServiceAsserts.isTrue(userEntity.getValid(), "账号被禁用");
        ServiceAsserts.isTrue(Objects.equals(userEntity.getPassword(), password), "密码错误");
        return buildResponse(userEntity);
    }

    @PostMapping("memo")
    public void updateMemo(@RequestBody UserRequest request) {
        UserEntity userEntity = new UserEntity();
        userEntity.setMemo(request.getMemo());
        UserQuery byEmail = UserQuery.builder().emailLike(request.getEmail()).memoNull(true).build();
        userService.patch(userEntity, byEmail);
    }

}
