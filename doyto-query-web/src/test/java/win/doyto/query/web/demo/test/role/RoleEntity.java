package win.doyto.query.web.demo.test.role;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.entity.CommonEntity;
import win.doyto.query.validation.CreateGroup;

import javax.validation.constraints.NotNull;

/**
 * UserEntity
 *
 * @author f0rb on 2020-04-01
 */
@Getter
@Setter
public class RoleEntity extends CommonEntity<Long, Long> {

    @NotNull(groups = CreateGroup.class)
    private String roleName;

    @NotNull(groups = CreateGroup.class)
    private String roleCode;

    private Boolean valid;
}
