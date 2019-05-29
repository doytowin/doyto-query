package win.doyto.query.user;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.entity.IntegerId;

import java.util.Date;
import java.util.LinkedList;
import javax.persistence.Column;
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
    @Column
    private String username;
    private String password;
    private String mobile;
    private String email;
    private String nickname;
    private String userLevel;
    private boolean valid = true;

    @Transient
    private Date createTime;

    private static final int INIT_SIZE = 5;

    public static LinkedList<UserEntity> initUserEntities() {
        LinkedList<UserEntity> userEntities = new LinkedList<>();

        for (int i = 1; i < INIT_SIZE; i++) {
            UserEntity userEntity = new UserEntity();
            userEntity.setId(i);
            userEntity.setUsername("username" + i);
            userEntity.setPassword("password" + i);
            userEntity.setEmail("test" + i + "@163.com");
            userEntity.setMobile("1777888888" + i);
            userEntity.setValid(i % 2 == 0);
            userEntities.add(userEntity);
        }
        UserEntity userEntity = new UserEntity();
        userEntity.setId(INIT_SIZE);
        userEntity.setUsername("f0rb");
        userEntity.setNickname("自在");
        userEntity.setPassword("123456");
        userEntity.setEmail("f0rb@163.com");
        userEntity.setMobile("17778888880");
        userEntity.setValid(true);
        userEntities.add(userEntity);
        return userEntities;
    }
}
