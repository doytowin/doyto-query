package win.doyto.query.web.demo.test.role;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import win.doyto.query.service.CrudService;
import win.doyto.query.web.controller.AbstractEIQController;
import win.doyto.query.web.response.JsonBody;

import java.util.List;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * UserController
 *
 * @author f0rb on 2020-04-01
 */
@JsonBody
@Validated
@RestController
@RequestMapping("role")
public class RoleController extends AbstractEIQController<RoleEntity, Long, RoleQuery> {

    @Resource
    private BeanFactory beanFactory;

    public CrudService<RoleEntity, Long, RoleQuery> getService() {
        return service;
    }

    @GetMapping("/roleName")
    public RoleEntity getByUsername(@Size(min = 4, max = 20) @NotNull String roleName) {
        return service.get(RoleQuery.builder().roleName(roleName).build());
    }

    @Override
    public void create(List<RoleEntity> requests) {
        super.create(requests);
        throw new DuplicateKeyException("");
    }
}
