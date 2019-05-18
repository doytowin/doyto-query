package win.doyto.query.module.user;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.entity.LongId;

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
class UserEntity extends LongId {
    public static final String TABLE = "user";
    private String username;
    private String password;
    private String mobile;
    private String email;
    private String nickname;
    private boolean valid;
}