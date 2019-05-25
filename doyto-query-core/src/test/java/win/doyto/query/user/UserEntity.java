package win.doyto.query.user;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.entity.IntegerId;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * UserEntity
 *
 * @author f0rb 2019-05-12
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
    private boolean valid = true;

    @Transient
    private Date createTime;
}
