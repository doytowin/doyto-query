package win.doyto.query.demo.module.user;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import win.doyto.query.web.controller.AbstractIQRSController;

/**
 * UserController
 *
 * @author f0rb on 2020-04-01
 */
@RestController
@RequestMapping("user2")
public class UserController2 extends AbstractIQRSController<UserEntity, Long, UserQuery, UserRequest, UserResponse> {

}
