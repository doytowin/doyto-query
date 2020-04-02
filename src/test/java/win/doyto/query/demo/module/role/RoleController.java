package win.doyto.query.demo.module.role;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import win.doyto.query.web.controller.AbstractIQEEController;

/**
 * UserController
 *
 * @author f0rb on 2020-04-01
 */
@RestController
@RequestMapping("role")
public class RoleController extends AbstractIQEEController<RoleEntity, Long, RoleQuery> {
}
