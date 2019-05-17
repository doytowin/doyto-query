package win.doyto.query.user;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.entity.IntegerId;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * UserEntity
 *
 * @author f0rb
 * @date 2019-05-12
 */
@Getter
@Setter
@Entity
@Table(name = "user")
public class UserEntity extends IntegerId {
    private String username;
    private String password;
    private String mobile;
    private String email;
    private String nickname;
    private String valid;
    private Date createTime;
}
