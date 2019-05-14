package win.doyto.query.user;

import lombok.Getter;
import lombok.Setter;

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
@Table(name = UserEntity.TABLE)
public class UserEntity {
    public static final String TABLE = "user";
    private Integer id;
    private String username;
    private String password;
    private String mobile;
    private String email;
    private String nickname;
    private String valid;
}
