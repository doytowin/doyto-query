package win.doyto.query.demo.module.user;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import win.doyto.query.entity.EntityResponse;

/**
 * UserResponse
 *
 * @author f0rb
 */
@Getter
@Setter
public class UserResponse implements EntityResponse<UserEntity, UserResponse> {
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

    @Override
    public UserResponse from(UserEntity userEntity) {
        UserResponse userResponse = new UserResponse();
        BeanUtils.copyProperties(userEntity, userResponse);
        return userResponse;
    }
}
