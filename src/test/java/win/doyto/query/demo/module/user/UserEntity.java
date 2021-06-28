package win.doyto.query.demo.module.user;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.entity.AbstractPersistable;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

/**
 * TestEntity
 *
 * @author f0rb
 */
@Getter
@Setter
@Entity
@Table(name = UserEntity.TABLE)
@SuppressWarnings("unused")
class UserEntity extends AbstractPersistable<Long> {
    public static final String TABLE = "user";
    private String username;
    private String password;
    private String mobile;
    private String email;
    private String nickname;
    private String memo;
    @Enumerated(EnumType.STRING)
    private UserLevel userLevel;
    private Boolean valid;
}