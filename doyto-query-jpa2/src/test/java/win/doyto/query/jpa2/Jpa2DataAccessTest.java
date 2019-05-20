package win.doyto.query.jpa2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import win.doyto.query.Jpa2App;
import win.doyto.query.test.user.UserEntity;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Jpa2DataAccessTest
 *
 * @author f0rb
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Jpa2App.class)
class Jpa2DataAccessTest {

    @Resource
    UserService userService;

    @Test
    @Transactional
    void fetch() {
        UserEntity u1 = userService.get(1);
        UserEntity u2 = userService.get(1);
        assertSame(u1, u2);

        UserEntity f1 = userService.fetch(1);
        assertNotSame(u1, f1);
    }
}