package win.doyto.query.core.test;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.entity.IntegerId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * TestEntity
 *
 * @author f0rb 2019-05-12
 */
@Getter
@Setter
@Entity
@Table(name = TestEntity.TABLE)
public class TestEntity extends IntegerId {
    public static final String TABLE = "user";
    @Column
    private String username;
    private String password;
    private String mobile;
    private String email;
    private String nickname;
    private String userLevel;
    private Boolean valid;

    @Transient
    private Date createTime;

    private static final int INIT_SIZE = 5;

    public static List<TestEntity> initUserEntities() {
        List<TestEntity> userEntities = new ArrayList<>(INIT_SIZE);

        for (int i = 1; i < INIT_SIZE; i++) {
            TestEntity testEntity = new TestEntity();
            testEntity.setId(i);
            testEntity.setUsername("username" + i);
            testEntity.setPassword("password" + i);
            testEntity.setEmail("test" + i + "@163.com");
            testEntity.setMobile("1777888888" + i);
            testEntity.setValid(i % 2 == 0);
            userEntities.add(testEntity);
        }
        TestEntity testEntity = new TestEntity();
        testEntity.setId(INIT_SIZE);
        testEntity.setUsername("f0rb");
        testEntity.setNickname("自在");
        testEntity.setPassword("123456");
        testEntity.setEmail("f0rb@163.com");
        testEntity.setMobile("17778888880");
        testEntity.setValid(true);
        userEntities.add(testEntity);
        return userEntities;
    }
}
