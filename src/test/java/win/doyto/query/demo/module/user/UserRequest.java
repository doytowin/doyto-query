package win.doyto.query.demo.module.user;

import lombok.Getter;
import lombok.Setter;

/**
 * UserRequest
 *
 * @author f0rb
 */
@Getter
@Setter
public class UserRequest {

    private String username;
    private String password;
    private String mobile;
    private String email;
    private String nickname;
    private String memo;
    private boolean valid = true;
    private UserLevel userLevel;
    private String address;

}
