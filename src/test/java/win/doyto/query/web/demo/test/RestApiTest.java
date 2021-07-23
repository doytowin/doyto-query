package win.doyto.query.web.demo.test;

import org.junit.jupiter.api.Test;
import win.doyto.query.web.demo.module.user.UserApi;
import win.doyto.query.web.demo.module.user.UserQuery;
import win.doyto.query.web.demo.module.user.UserRequest;
import win.doyto.query.web.demo.module.user.UserResponse;

import java.util.List;
import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * RestApiTest
 *
 * @author f0rb on 2021-07-17
 */
class RestApiTest extends DemoApplicationTest {

    @Resource
    UserApi userApi;

    @Test
    void create() throws Exception {
        UserRequest request = new UserRequest();
        request.setUsername("test");
        request.setPassword("test");
        userApi.create(request);
        performAndExpectSuccess(get("/user/"))
                .andExpect(jsonPath("$.data.total").value(5))
        ;
    }

    @Test
    void query() {
        List<UserResponse> userResponses = userApi.query(UserQuery.builder().build());
        assertEquals(4, userResponses.size());
    }
}
