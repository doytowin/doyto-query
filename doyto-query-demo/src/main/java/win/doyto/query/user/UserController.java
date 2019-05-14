package win.doyto.query.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import win.doyto.query.core.PageList;

import java.util.List;
import javax.annotation.Resource;

/**
 * UserController
 *
 * @author f0rb
 * @date 2019-05-12
 */
@Slf4j
@RestController
@RequestMapping("user")
class UserController {

    @Resource
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
    public UserResponse get(Integer id) {
        return UserResponse.of(userService.get(id));
    }

    @PostMapping("save")
    public void save(@RequestBody UserRequest userRequest) {
        userService.save(userRequest.toEntity());
    }

}
