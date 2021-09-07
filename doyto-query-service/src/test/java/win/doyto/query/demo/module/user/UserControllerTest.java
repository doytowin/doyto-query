package win.doyto.query.demo.module.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import win.doyto.query.service.PageList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * UserControllerTest
 *
 * @author f0rb
 */
class UserControllerTest {
    public UserApi userApi;

    @BeforeEach
    void setUp() {
        this.userApi = UserData.getUserController();
    }

    @Test
    void query() {
        UserQuery userQuery = UserQuery.builder().username("username1").build();
        assertThat(userApi.query(userQuery))
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
        PageList<UserResponse> page = userApi.page(userQuery);
        assertEquals(UserData.INIT_SIZE, page.getTotal());
        assertEquals(2, page.getList().size());
    }

    @Test
    void pageUserWithCriteria() {
        UserQuery userQuery = UserQuery.builder().usernameLike("username").build();
        userQuery.setPageNumber(1).setPageSize(2);
        PageList<UserResponse> page = userApi.page(userQuery);
        assertEquals(4, page.getTotal());
        assertThat(page.getList()).hasSize(2).extracting(UserResponse::getId).containsExactly(3L, 4L);
    }

    @Test
    void get() {
        assertThat(userApi.get(1L))
            .hasFieldOrPropertyWithValue("id", 1L)
            .hasFieldOrPropertyWithValue("username", "username1")
        ;
    }

    @Test
    void delete() {
        userApi.delete(1L);
        assertThat(userApi.page(UserQuery.builder().build()).getTotal()).isEqualTo(4);
    }

    @Test
    void supportInheritanceOnConcreteSubClassOfAbstractService() {
        assertNotNull(new UserController(new UserService(), new UserDetailService()) {});
    }

    @Test
    void forcePage() {
        UserQuery userQuery = UserQuery.builder().build();
        userApi.page(userQuery);
        assertEquals(0, (int) userQuery.getPageNumber());
    }
}