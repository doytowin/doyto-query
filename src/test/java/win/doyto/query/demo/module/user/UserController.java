package win.doyto.query.demo.module.user;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import win.doyto.query.web.controller.AbstractRestController;

import javax.annotation.Resource;

/**
 * UserController
 *
 * @author f0rb on 2020-04-01
 */
@RestController
@RequestMapping("user")
public class UserController extends AbstractRestController<UserEntity, Long, UserQuery, UserRequest, UserResponse> {

    @Resource
    private UserService userService;

    @Override
    protected UserService getService() {
        return userService;
    }

}
