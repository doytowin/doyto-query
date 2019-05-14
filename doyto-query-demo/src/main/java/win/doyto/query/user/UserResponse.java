package win.doyto.query.user;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

/**
 * UserResponse
 *
 * @author f0rb
 * @date 2019-05-14
 */
@Getter
@Setter
public class UserResponse {
    private Integer id;
    private String username;
    private String mobile;
    private String email;
    private String nickname;
    private Boolean valid;

    public static UserResponse of(UserEntity userEntity) {
        UserResponse userResponse = new UserResponse();
        BeanUtils.copyProperties(userEntity, userResponse);
        return userResponse;
    }
}
