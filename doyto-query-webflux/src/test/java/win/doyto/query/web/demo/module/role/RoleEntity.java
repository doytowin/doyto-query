package win.doyto.query.web.demo.module.role;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.entity.AbstractPersistable;

import javax.persistence.Table;

/**
 * RoleEntity
 *
 * @author f0rb on 2021-10-26
 */
@Getter
@Setter
@Table(name = "t_role")
public class RoleEntity extends AbstractPersistable<Integer> {

    private String roleName;

    private String roleCode;

    private Boolean valid;

}
