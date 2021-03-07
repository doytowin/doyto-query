package win.doyto.query.web.demo.module.user;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.entity.CommonEntity;
import win.doyto.query.web.validation.CreateGroup;

import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * UserEntity
 *
 * @author f0rb on 2020-04-01
 */
@Table(name = "t_user")
@Getter
@Setter
public class UserEntity extends CommonEntity<Long, Long> {

    @NotNull(groups = CreateGroup.class)
    private String username;
    private String email;
    private String mobile;

    @NotNull(groups = CreateGroup.class)
    private String password;
    private String nickname;
    private Boolean valid;
}
