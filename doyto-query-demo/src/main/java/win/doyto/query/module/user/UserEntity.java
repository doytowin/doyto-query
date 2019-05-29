package win.doyto.query.module.user;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.entity.LongId;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
@SuppressWarnings("unused")
class UserEntity extends LongId {
    public static final String TABLE = "user";
    private String username;
    private String password;
    private String mobile;
    private String email;
    private String nickname;
    @Enumerated(EnumType.STRING)
    private UserLevel userLevel;
    private boolean valid;
}