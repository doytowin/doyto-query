package win.doyto.query.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
public class UserController {

    @Resource
    UserService userService;

    @GetMapping("query")
    public List<UserEntity> query(UserQuery userQuery) {
        return userService.query(userQuery);
    }

}
