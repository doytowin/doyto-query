package win.doyto.query.web.demo.module.role;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.entity.CommonEntity;
import win.doyto.query.validation.CreateGroup;

import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * UserEntity
 *
 * @author f0rb on 2020-04-01
 */
@Table(name = "t_role")
@Getter
@Setter
public class RoleEntity extends CommonEntity<Long, Long> {

    @NotNull(groups = CreateGroup.class)
    private String roleName;

    @NotNull(groups = CreateGroup.class)
    private String roleCode;

    private Boolean valid;
}
