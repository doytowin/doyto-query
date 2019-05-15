package win.doyto.query.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import win.doyto.query.core.PageList;

import java.util.LinkedList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * UserControllerTest
 *
 * @author f0rb
 * @date 2019-05-15
 */
class UserControllerTest {
    public static final int INIT_SIZE = 5;
    private final UserService userService = new UserService(new MockUserRepository());
    UserController userController = new UserController(userService);

    @BeforeEach
    void setUp() {
        LinkedList<UserEntity> userEntities = new LinkedList<>();
        for (int i = 1; i <= INIT_SIZE; i++) {
            UserEntity userEntity = new UserEntity();
            userEntity.setUsername("username" + i);
            userEntity.setPassword("password" + i);
            userEntity.setEmail("test" + i + "@163.com");
            userEntity.setMobile("1777888888" + i);
            userEntities.add(userEntity);
        }
        userService.save(userEntities);
    }

    @Test
    void query() {
        assertThat(userController.query(UserQuery.builder().username("username1").build())).hasSize(1)
            .first()
            .hasFieldOrPropertyWithValue("id", 1)
            .hasFieldOrPropertyWithValue("username", "username1")
        ;
    }

    @Test
    void page() {
        UserQuery userQuery = UserQuery.builder().build();
        userQuery.setPageNumber(0).setPageSize(2);
        PageList<UserResponse> page = userController.page(userQuery);
        assertEquals(INIT_SIZE, page.getTotal());
        assertEquals(2, page.getList().size());
    }

    @Test
    void get() {
        assertThat(userController.get(1))
            .hasFieldOrPropertyWithValue("id", 1)
            .hasFieldOrPropertyWithValue("username", "username1")
        ;
    }

    @Test
    void delete() {
        userController.delete(1);
        assertThat(userController.page(UserQuery.builder().build()).getTotal()).isEqualTo(4);
    }
}