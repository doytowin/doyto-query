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
import org.springframework.test.web.servlet.ResultActions;
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
    private MockHttpSession session;


    private ResultActions postJson(String url, String content, MockHttpSession session) throws Exception {
        return mockMvc.perform(post(url).content(content).contentType(MediaType.APPLICATION_JSON_UTF8).session(session));
    }

    private ResultActions postJson(String url, String content) throws Exception {
        return postJson(url, content, new MockHttpSession());
    }

    @BeforeEach
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

        MvcResult mvcResult = postJson("/login", "{\"account\":\"f0rb\",\"password\":\"123456\"}").andExpect(status().is(200)).andReturn();
        session = (MockHttpSession) mvcResult.getRequest().getSession();
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
        postJson("/user/save", "{\"username\": \"test\"}");

        mockMvc.perform(get("/user/page"))
               .andDo(print())
               .andExpect(jsonPath("$.total").value(5))
               .andExpect(jsonPath("$.list[4].username").value("test"))
        ;
    }

    @Test
    public void updateUser() throws Exception {
        String result = mockMvc.perform(get("/user/get?id=1")).andReturn().getResponse().getContentAsString();

        postJson("/user/save", result.replace("f0rb", "test"));

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

    @Test
    public void saveMenu() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/menu/get?id=1"))
                                     .andExpect(jsonPath("$.updateUserId").doesNotExist())
                                     .andReturn();
        String json = mvcResult.getResponse().getContentAsString();
        postJson("/menu/save", json, session);
        mockMvc.perform(get("/menu/get?id=1"))
               .andExpect(jsonPath("$.updateUserId").value("1"));
    }

    /*=============== login ==================*/
    @Test
    void login() throws Exception {
        MvcResult mvcResult = postJson("/login", "{\"account\":\"f0rb\",\"password\":\"123456\"}").andExpect(status().is(200)).andReturn();
        MockHttpSession session = (MockHttpSession) mvcResult.getRequest().getSession();
        mockMvc.perform(get("/account").session(session))
               .andExpect(jsonPath("$.id").value("1"))
               .andExpect(jsonPath("$.nickname").value("测试1"))
        ;
    }
}