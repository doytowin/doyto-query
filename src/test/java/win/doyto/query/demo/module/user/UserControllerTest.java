package win.doyto.query.demo.module.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import win.doyto.query.service.PageList;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * UserControllerTest
 *
 * @author f0rb
 */
public class UserControllerTest {
    public static final int INIT_SIZE = 5;
    public static UserService userService;

    static {
        initData().forEach(userService::create);
    }

    private static List<UserRequest> initData() {
        UserController userController = new UserController();
        userController.userDetailService = new UserDetailService();

        UserControllerTest.userService = userController;
        List<UserRequest> userRequests = new ArrayList<>(INIT_SIZE);
        for (int i = 1; i < INIT_SIZE; i++) {
            UserRequest userRequest = new UserRequest();
            userRequest.setUsername("username" + i);
            userRequest.setPassword("password" + i);
            userRequest.setEmail("test" + i + "@163.com");
            userRequest.setMobile("1777888888" + i);
            userRequest.setValid(i % 2 == 0);
            userRequests.add(userRequest);
        }
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername("f0rb");
        userRequest.setNickname("自在");
        userRequest.setPassword("123456");
        userRequest.setEmail("f0rb@163.com");
        userRequest.setMobile("17778888880");
        userRequests.add(userRequest);
        return userRequests;
    }

    @BeforeEach
    void setUp() {
        initData().forEach(userService::create);
    }

    @Test
    void query() {
        UserQuery userQuery = UserQuery.builder().username("username1").build();
        assertThat(userService.list(userQuery))
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
        PageList<UserResponse> page = userService.page(userQuery);
        assertEquals(INIT_SIZE, page.getTotal());
        assertEquals(2, page.getList().size());
    }

    @Test
    void pageUserWithCriteria() {
        UserQuery userQuery = UserQuery.builder().usernameLike("username").build();
        userQuery.setPageNumber(1).setPageSize(2);
        PageList<UserResponse> page = userService.page(userQuery);
        assertEquals(4, page.getTotal());
        assertThat(page.getList()).hasSize(2).extracting(UserResponse::getId).containsExactly(3L, 4L);
    }

    @Test
    void get() {
        assertThat(userService.getById(1L))
            .hasFieldOrPropertyWithValue("id", 1L)
            .hasFieldOrPropertyWithValue("username", "username1")
        ;
    }

    @Test
    void delete() {
        userService.deleteById(1L);
        assertThat(userService.page(UserQuery.builder().build()).getTotal()).isEqualTo(4);
    }

    @Test
    void supportInheritanceOnConcreteSubClassOfAbstractService() {
        assertNotNull(new UserController() {});
    }

    @Test
    void forcePage() {
        UserQuery userQuery = UserQuery.builder().build();
        userService.page(userQuery);
        assertEquals(0, (int) userQuery.getPageNumber());
    }
}