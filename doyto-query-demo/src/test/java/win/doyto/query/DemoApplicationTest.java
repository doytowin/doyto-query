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
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import win.doyto.query.module.user.TestUserEntityAspect;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * DemoApplicationTest
 *
 * @author f0rb
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

    private ResultActions requestJson(MockHttpServletRequestBuilder builder, String content, MockHttpSession session) throws Exception {
        return mockMvc.perform(builder.content(content).contentType(MediaType.APPLICATION_JSON_UTF8).session(session));
    }

    private ResultActions requestJson(MockHttpServletRequestBuilder builder, String content) throws Exception {
        return requestJson(builder, content, new MockHttpSession());
    }

    @BeforeEach
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

        MvcResult mvcResult = requestJson(post("/login"), "{\"account\":\"f0rb\",\"password\":\"123456\"}").andExpect(statusIs200()).andReturn();
        session = (MockHttpSession) mvcResult.getRequest().getSession();
    }

    /*=============== user ==================*/
    private static final String URL_USER = "/user/";
    private static final String URL_USER_1 = URL_USER + "1";

    @Test
    public void queryByUsername() throws Exception {
        mockMvc.perform(get(URL_USER + "?username=f0rb"))
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$[0].nickname").value("测试1"))
        ;
    }

    @Test
    public void queryByAccount() throws Exception {
        mockMvc.perform(get(URL_USER + "?account=17778888882"))
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$[0].nickname").value("测试2"))
               .andExpect(jsonPath("$[0].password").doesNotExist())
               .andExpect(jsonPath("$[1]").doesNotExist())
        ;
    }

    @Test
    public void pageByAccount() throws Exception {
        mockMvc.perform(get(URL_USER + "?account=17778888882&pageNumber=0&pageSize=5"))
               .andExpect(jsonPath("$.list").isArray())
               .andExpect(jsonPath("$.list[0].nickname").value("测试2"))
               .andExpect(jsonPath("$.list[0].password").doesNotExist())
               .andExpect(jsonPath("$.total").value(1))
        ;
    }

    @Test
    public void pageByUsernameLike() throws Exception {
        mockMvc.perform(get(URL_USER + "?usernameLike=user&pageNumber=0&pageSize=2"))
               .andDo(print())
               .andExpect(jsonPath("$.list").isArray())
               .andExpect(jsonPath("$.list[0].nickname").value("测试2"))
               .andExpect(jsonPath("$.list[0].password").doesNotExist())
               .andExpect(jsonPath("$.total").value(3))
        ;
    }

    @Test
    public void validateSortField() throws Exception {
        mockMvc.perform(get(URL_USER + "?sort=username")).andExpect(status().is(400));
        mockMvc.perform(get(URL_USER + "?sort=username,asc")).andExpect(status().is(200));
    }

    @Test
    public void getUserById() throws Exception {
        mockMvc.perform(get(URL_USER_1))
               .andExpect(jsonPath("$.username").value("f0rb"))
               .andExpect(jsonPath("$.nickname").value("测试1"))
               .andExpect(jsonPath("$.password").doesNotExist())
        ;
    }

    @Test
    public void createUser() throws Exception {
        requestJson(post(URL_USER), "{\"username\": \"test\"}");

        mockMvc.perform(get(URL_USER + "?pageNumber=0"))
               .andDo(print())
               .andExpect(jsonPath("$.total").value(5))
               .andExpect(jsonPath("$.list[4].username").value("test"))
        ;
    }

    @Resource
    TestUserEntityAspect testUserEntityAspect;

    @Test
    public void updateUser() throws Exception {
        String result = mockMvc.perform(get(URL_USER_1)).andReturn().getResponse().getContentAsString();

        int timesBefore = testUserEntityAspect.getTimes();
        requestJson(put(URL_USER_1), result.replace("f0rb", "test"));
        assertEquals(1, testUserEntityAspect.getTimes() - timesBefore);

        mockMvc.perform(get(URL_USER + "?pageNumber=0"))
               .andDo(print())
               .andExpect(jsonPath("$.list[0].username").value("test"))
               .andExpect(jsonPath("$.total").value(4))
        ;
    }

    @Test
    public void patchUser() throws Exception {
        requestJson(patch(URL_USER_1), "{\"id\":1,\"username\":\"test\"}");

        mockMvc.perform(get(URL_USER_1))
               .andDo(print())
               .andExpect(jsonPath("$.username").value("test"))
               .andExpect(jsonPath("$.nickname").value("测试1"))
        ;
    }

    /*=============== menu ==================*/
    private String menuUri = "/01/menu/";
    private String menuUri1 = menuUri + "1";

    @Test
    public void pageMenu() throws Exception {
        mockMvc.perform(get(menuUri + "?pageNumber=1&pageSize=2"))
               .andExpect(statusIs200())
               .andExpect(jsonPath("$.list").isArray())
               .andExpect(jsonPath("$.list[0]").doesNotExist())
               .andExpect(jsonPath("$.total").value(2))
        ;
    }

    @Test
    public void getMenuById() throws Exception {
        mockMvc.perform(get(menuUri1))
               .andDo(print())
               .andExpect(statusIs200())
               .andExpect(jsonPath("$.id").value("1"))
               .andExpect(jsonPath("$.menuName").value("root"))
        ;
    }

    @Test
    public void saveMenu() throws Exception {
        MvcResult mvcResult = mockMvc
            .perform(get(menuUri1))
            .andExpect(statusIs200())
            .andExpect(jsonPath("$.updateUserId").doesNotExist())
            .andReturn();
        String json = mvcResult.getResponse().getContentAsString();
        requestJson(put(menuUri1), json, session);
        mockMvc.perform(get(menuUri1))
               .andExpect(statusIs200())
               .andExpect(jsonPath("$.updateUserId").value("1"));
    }

    @Test
    public void createMenu() throws Exception {
        requestJson(post(menuUri), "{\"platform\":\"01\", \"menuName\":\"Test Menu\"}", session)
            .andExpect(statusIs200());

        mockMvc.perform(get(menuUri + "3"))
               .andExpect(statusIs200())
               .andExpect(jsonPath("$.createUserId").value("1"))
               .andExpect(jsonPath("$.updateUserId").doesNotExist());

        mockMvc.perform(delete(menuUri + "3")).andExpect(statusIs200());

        mockMvc.perform(get(menuUri + "?pageNumber=0"))
               .andExpect(statusIs200())
               .andExpect(jsonPath("$.list[1]").exists())
               .andExpect(jsonPath("$.list[2]").doesNotExist());
    }

    private ResultMatcher statusIs200() {
        return status().is(200);
    }

    /*=============== login ==================*/
    @Test
    void login() throws Exception {
        MvcResult mvcResult = requestJson(post("/login"), "{\"account\":\"f0rb\",\"password\":\"123456\"}").andExpect(statusIs200()).andReturn();
        MockHttpSession session = (MockHttpSession) mvcResult.getRequest().getSession();
        mockMvc.perform(get("/account").session(session))
               .andExpect(jsonPath("$.id").value("1"))
               .andExpect(jsonPath("$.nickname").value("测试1"))
        ;
    }
}