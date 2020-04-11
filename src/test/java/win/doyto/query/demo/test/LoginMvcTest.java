package win.doyto.query.demo.test;

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
public class LoginMvcTest extends DemoApplicationTest {
    /*=============== login ==================*/
    @Test
    void login() throws Exception {
        String content = "{\"account\":\"f0rb\",\"password\":\"123456\"}";
        MvcResult mvcResult = requestJson(post("/login"), content).andExpect(statusIs200()).andReturn();
        MockHttpSession session = (MockHttpSession) mvcResult.getRequest().getSession();
        mockMvc.perform(get("/account").session(session))
               .andExpect(jsonPath("$.id").value("1"))
               .andExpect(jsonPath("$.nickname").value("测试1"))
        ;
    }
}
