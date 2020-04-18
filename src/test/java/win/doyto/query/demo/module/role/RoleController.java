package win.doyto.query.demo.module.role;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import win.doyto.query.web.controller.AbstractIQEEController;

/**
 * RoleController
 *
 * @author f0rb on 2019-05-28
 */
@Slf4j
@RestController
@RequestMapping("role")
public class RoleController extends AbstractIQEEController<RoleEntity, Long, RoleQuery> {

}
