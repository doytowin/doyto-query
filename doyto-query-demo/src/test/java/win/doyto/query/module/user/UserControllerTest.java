package win.doyto.query.module.user;

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
public class UserControllerTest {
    public static final int INIT_SIZE = 5;
    private static final MockUserRepository userRepository = new MockUserRepository();
    private static final UserService userService = new UserService(userRepository);
    public static UserController userController = new UserController(userService);

    static {
        initData();
    }

    private static void initData() {
        LinkedList<UserEntity> userEntities = new LinkedList<>();
        for (int i = 1; i < INIT_SIZE; i++) {
            UserEntity userEntity = new UserEntity();
            userEntity.setUsername("username" + i);
            userEntity.setPassword("password" + i);
            userEntity.setEmail("test" + i + "@163.com");
            userEntity.setMobile("1777888888" + i);
            userEntities.add(userEntity);
        }
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("f0rb");
        userEntity.setNickname("自在");
        userEntity.setPassword("123456");
        userEntity.setEmail("f0rb@163.com");
        userEntity.setMobile("17778888880");
        userEntity.setValid(true);
        userEntities.add(userEntity);
        userService.save(userEntities);
    }

    @BeforeEach
    void setUp() {
        userRepository.reset();
        initData();
    }

    @Test
    void query() {
        UserQuery userQuery = UserQuery.builder().username("username1").build();
        assertThat(userController.query(userQuery))
            .hasSize(1)
            .first()
            .hasFieldOrPropertyWithValue("id", 1L)
            .hasFieldOrPropertyWithValue("username", "username1")
        ;
    }

    @Test
    void pageUser() {
        UserQuery userQuery = UserQuery.builder().build();
        userQuery.setPageNumber(0).setPageSize(2);
        PageList<UserResponse> page = userController.page(userQuery);
        assertEquals(INIT_SIZE, page.getTotal());
        assertEquals(2, page.getList().size());
    }

    @Test
    void pageUserWithCriteria() {
        UserQuery userQuery = UserQuery.builder().usernameLike("username").build();
        userQuery.setPageNumber(1).setPageSize(2);
        PageList<UserResponse> page = userController.page(userQuery);
        assertEquals(4, page.getTotal());
        assertEquals(2, page.getList().size());
    }

    @Test
    void get() {
        assertThat(userController.get(1L))
            .hasFieldOrPropertyWithValue("id", 1L)
            .hasFieldOrPropertyWithValue("username", "username1")
        ;
    }

    @Test
    void delete() {
        userController.delete(1L);
        assertThat(userController.page(UserQuery.builder().build()).getTotal()).isEqualTo(4);
    }
}