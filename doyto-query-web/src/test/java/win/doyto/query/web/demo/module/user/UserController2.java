package win.doyto.query.web.demo.module.user;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import win.doyto.query.web.controller.AbstractRestController;

/**
 * UserController
 *
 * @author f0rb on 2020-04-01
 */
@RestController
@RequestMapping("user2")
public class UserController2 extends AbstractRestController<UserEntity, Long, UserQuery, UserEntity, UserEntity> {

}
