package win.doyto.query.module.user;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

/**
 * UserRequest
 *
 * @author f0rb
 * @date 2019-05-14
 */
@Getter
@Setter
public class UserRequest {

    private Long id;
    private String username;
    private String password;
    private String mobile;
    private String email;
    private String nickname;

    public UserEntity toEntity() {
        UserEntity userEntity = new UserEntity();
        BeanUtils.copyProperties(this, userEntity);
        userEntity.setValid(true);
        return userEntity;
    }
}
