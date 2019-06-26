package win.doyto.query.demo.module.user;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import win.doyto.query.entity.EntityRequest;

/**
 * UserRequest
 *
 * @author f0rb
 */
@Getter
@Setter
public class UserRequest implements EntityRequest<UserEntity> {

    private Long id;
    private String username;
    private String password;
    private String mobile;
    private String email;
    private String nickname;
    private String memo;
    private boolean valid = true;
    private UserLevel userLevel;
    private String address;

    @Override
    public UserEntity toEntity() {
        UserEntity userEntity = new UserEntity();
        BeanUtils.copyProperties(this, userEntity);
        return userEntity;
    }
}
