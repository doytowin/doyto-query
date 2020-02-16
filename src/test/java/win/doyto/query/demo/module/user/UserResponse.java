package win.doyto.query.demo.module.user;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * UserResponse
 *
 * @author f0rb
 */
@Getter
@Setter
public class UserResponse implements Serializable {
    private static final long serialVersionUID = -1L;

    private Long id;
    private String username;
    private String mobile;
    private String email;
    private String nickname;
    private String memo;
    private Boolean valid;
    private UserLevel userLevel;
    private String address;

}
