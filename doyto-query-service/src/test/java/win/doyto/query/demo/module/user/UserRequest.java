package win.doyto.query.demo.module.user;

import com.fasterxml.jackson.annotation.JsonInclude;
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

    private Long id;
    private String username;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String password;
    private String mobile;
    private String email;
    private String nickname;
    private String memo;
    private boolean valid = true;
    private UserLevel userLevel;
    private String address;

}
