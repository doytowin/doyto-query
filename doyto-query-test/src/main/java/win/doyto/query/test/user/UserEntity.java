package win.doyto.query.test.user;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.entity.IntegerId;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * UserEntity
 *
 * @author f0rb
 */
@Getter
@Setter
@Entity
@Table(name = UserEntity.TABLE)
public class UserEntity extends IntegerId {
    public static final String TABLE = "user";
    private String username;
    private String password;
    private String mobile;
    private String email;
    private String nickname;
    private String valid;
}
