package win.doyto.query.web.demo.test;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * LoginTest
 *
 * @author f0rb on 2020-04-11
 */
class LoginMvcTest extends DemoApplicationTest {
    /*=============== login ==================*/
    @Test
    void login() throws Exception {
        String content = "{\"account\":\"f0rb\",\"password\":\"123456\"}";
        MvcResult mvcResult = performAndExpectSuccess(post("/login"), content).andReturn();
        MockHttpSession session = (MockHttpSession) mvcResult.getRequest().getSession();
        mockMvc.perform(get("/account").session(session))
               .andExpect(jsonPath("$.data.id").value("1"))
               .andExpect(jsonPath("$.data.nickname").value("测试1"))
        ;
    }
}
