package win.doyto.query;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * DemoApplicationTest
 *
 * @author f0rb
 * @date 2019-05-12
 */
@Transactional
@Rollback
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest(classes = DemoApplication.class)
class DemoApplicationTest {

    @Resource
    protected WebApplicationContext wac;
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    /*=============== user ==================*/
    @Test
    public void queryByUsername() throws Exception {
        mockMvc.perform(get("/user/query?username=f0rb"))
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$[0].nickname").value("测试1"))
        ;
    }

    @Test
    public void queryByAccount() throws Exception {
        mockMvc.perform(get("/user/query?account=17778888882"))
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$[0].nickname").value("测试2"))
               .andExpect(jsonPath("$[0].password").doesNotExist())
               .andExpect(jsonPath("$[1]").doesNotExist())
        ;
    }

    @Test
    public void pageByAccount() throws Exception {
        mockMvc.perform(get("/user/page?account=17778888882&pageNumber=0&pageSize=5"))
               .andExpect(jsonPath("$.list").isArray())
               .andExpect(jsonPath("$.list[0].nickname").value("测试2"))
               .andExpect(jsonPath("$.list[0].password").doesNotExist())
               .andExpect(jsonPath("$.total").value(1))
        ;
    }

    @Test
    public void pageByUsernameLike() throws Exception {
        mockMvc.perform(get("/user/page?usernameLike=user&pageNumber=0&pageSize=2"))
               .andDo(print())
               .andExpect(jsonPath("$.list").isArray())
               .andExpect(jsonPath("$.list[0].nickname").value("测试2"))
               .andExpect(jsonPath("$.list[0].password").doesNotExist())
               .andExpect(jsonPath("$.total").value(3))
        ;
    }

    @Test
    public void getUserById() throws Exception {
        mockMvc.perform(get("/user/get?id=1"))
               .andExpect(jsonPath("$.username").value("f0rb"))
               .andExpect(jsonPath("$.nickname").value("测试1"))
               .andExpect(jsonPath("$.password").doesNotExist())
        ;
    }

    @Test
    public void createUser() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = post("/user/save")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content("{\"username\": \"test\"}");
        mockMvc.perform(requestBuilder);

        mockMvc.perform(get("/user/page"))
               .andDo(print())
               .andExpect(jsonPath("$.total").value(5))
               .andExpect(jsonPath("$.list[4].username").value("test"))
        ;
    }

    @Test
    public void updateUser() throws Exception {
        String result = mockMvc.perform(get("/user/get?id=1")).andReturn().getResponse().getContentAsString();

        MockHttpServletRequestBuilder requestBuilder = post("/user/save")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(result.replace("f0rb", "test"));
        mockMvc.perform(requestBuilder);

        mockMvc.perform(get("/user/page"))
               .andDo(print())
               .andExpect(jsonPath("$.list[0].username").value("test"))
               .andExpect(jsonPath("$.total").value(4))
        ;
    }

    /*=============== menu ==================*/
    @Test
    public void pageMenu() throws Exception {
        mockMvc.perform(get("/menu/page?pageNumber=1&pageSize=2"))
               .andExpect(jsonPath("$.list").isArray())
               .andExpect(jsonPath("$.list[0]").doesNotExist())
               .andExpect(jsonPath("$.total").value(2))
        ;
    }

    @Test
    public void getMenuById() throws Exception {
        mockMvc.perform(get("/menu/get?id=1"))
               .andExpect(jsonPath("$.id").value("1"))
               .andExpect(jsonPath("$.menuName").value("root"))
        ;
    }

    /*=============== login ==================*/
    @Test
    void login() throws Exception {
        MockHttpServletRequestBuilder loginRequest = post("/login")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content("{\"account\":\"f0rb\",\"password\":\"123456\"}");
        MvcResult mvcResult = mockMvc.perform(loginRequest).andExpect(status().is(200)).andReturn();
        MockHttpSession session = (MockHttpSession) mvcResult.getRequest().getSession();
        mockMvc.perform(get("/account").session(session))
               .andExpect(jsonPath("$.id").value("1"))
               .andExpect(jsonPath("$.nickname").value("测试1"))
        ;
    }
}