package win.doyto.query.web.demo.module.user;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.web.validation.CreateGroup;
import win.doyto.query.web.validation.PatchGroup;
import win.doyto.query.web.validation.UpdateGroup;

import javax.validation.constraints.NotNull;

/**
 * UserRequest
 *
 * @author f0rb on 2020-04-02
 */
@Getter
@Setter
public class UserRequest {

    @NotNull(groups = {UpdateGroup.class, PatchGroup.class})
    private Long id;

    @NotNull(groups = CreateGroup.class)
    private String username;
    private String email;
    private String mobile;

    @NotNull(groups = CreateGroup.class)
    private String password;
    private String nickname;
    private Boolean valid = true;
}
