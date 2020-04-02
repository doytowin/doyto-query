package win.doyto.query.demo.module.user;

import lombok.Getter;
import lombok.Setter;

/**
 * UserResponse
 *
 * @author f0rb on 2020-04-02
 */
@Getter
@Setter
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String mobile;
    private String nickname;
    private Boolean valid;
}
