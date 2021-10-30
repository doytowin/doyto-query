package win.doyto.query.web.demo.module.role;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import win.doyto.query.web.controller.ReactiveEIQController;

/**
 * RoleController
 *
 * @author f0rb on 2021-10-26
 */
@RestController
@RequestMapping("role")
public class RoleController extends ReactiveEIQController<RoleEntity, Integer, RoleQuery> {
}
