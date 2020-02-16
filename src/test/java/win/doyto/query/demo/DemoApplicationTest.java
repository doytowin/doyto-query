package win.doyto.query.demo;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.support.NoOpCache;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcOperations;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import win.doyto.query.cache.CacheWrapper;
import win.doyto.query.core.JoinQueryExecutor;
import win.doyto.query.core.test.TestJoinQuery;
import win.doyto.query.core.test.TestJoinView;
import win.doyto.query.core.test.UserCountByRoleView;
import win.doyto.query.demo.exception.ServiceException;
import win.doyto.query.demo.module.role.RoleController;
import win.doyto.query.demo.module.user.TestUserEntityAspect;
import win.doyto.query.service.AssociativeService;
import win.doyto.query.service.PageList;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Resource;
import javax.persistence.EntityNotFoundException;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    private JdbcOperations jdbcOperations;

    @Resource
    public void setJdbcOperations(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

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
    private static final String URL_USER_2 = URL_USER + "2";

    @Test
    public void queryByUsername() throws Exception {
        mockMvc.perform(get(URL_USER + "?username=f0rb"))
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$[0].nickname").value("测试1"))
               .andExpect(jsonPath("$[0].userLevel").value("高级"))
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
        requestJson(post(URL_USER), "{\"username\": \"test\",\"userLevel\": \"普通\"}");

        mockMvc.perform(get(URL_USER + "?pageNumber=0"))
               .andDo(print())
               .andExpect(jsonPath("$.total").value(5))
               .andExpect(jsonPath("$.list[4].username").value("test"))
               .andExpect(jsonPath("$.list[4].userLevel").value("普通"))
        ;
    }

    @Test
    public void createUserAndDetail() throws Exception {
        requestJson(post(URL_USER), "{\"username\": \"test\",\"userLevel\": \"普通\",\"address\": \"上海市\"}");

        mockMvc.perform(get(URL_USER + "5"))
               .andDo(print())
               .andExpect(jsonPath("$.username").value("test"))
               .andExpect(jsonPath("$.userLevel").value("普通"))
               .andExpect(jsonPath("$.address").value("上海市"))
        ;
    }

    @Test
    public void querySingleColumn() throws Exception {
        mockMvc.perform(get(URL_USER + "column/username"))
               .andDo(print())
               .andExpect(content().string("[\"f0rb\",\"user2\",\"user3\",\"user4\"]"));
        mockMvc.perform(get(URL_USER + "column/nickname"))
               .andDo(print())
               .andExpect(content().string("[\"测试1\",\"测试2\",\"测试3\",\"测试4\"]"));
    }

    @Test
    public void queryColumns() throws Exception {
        mockMvc.perform(get(URL_USER + "columns/username,userLevel"))
               .andDo(print())
               .andExpect(jsonPath("$.size()").value(4))
               .andExpect(jsonPath("$[0].USERNAME").value("f0rb"))
               .andExpect(jsonPath("$[1].USERNAME").value("user2"))
        ;

    }

    @Resource
    TestUserEntityAspect testUserEntityAspect;

    @Test
    public void updateUser() throws Exception {
        String result = mockMvc.perform(get(URL_USER_1)).andReturn().getResponse().getContentAsString();

        int timesBefore = testUserEntityAspect.getTimes();
        requestJson(put(URL_USER_1), result.replace("f0rb", "test"));
        Assertions.assertEquals(1, testUserEntityAspect.getTimes() - timesBefore);

        mockMvc.perform(get(URL_USER + "?pageNumber=0"))
               .andDo(print())
               .andExpect(jsonPath("$.list[0].username").value("test"))
               .andExpect(jsonPath("$.list[0].userLevel").value("高级"))
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

    @Test
    public void patchMemo() throws Exception {
        requestJson(post("/user/memo"), "{\"email\":\"qq\",\"memo\":\"qq邮箱\"}");

        mockMvc.perform(get(URL_USER))
               .andDo(print())
               .andExpect(jsonPath("$[0].email").value("f0rb@163.com"))
               .andExpect(jsonPath("$[0].memo").doesNotExist())
               .andExpect(jsonPath("$[1].email").value("test2@qq.com"))
               .andExpect(jsonPath("$[1].memo").value("qq邮箱"))
               .andExpect(jsonPath("$[2].email").value("test3@qq.com"))
               .andExpect(jsonPath("$[2].memo").value("memo"))
        ;
    }

    @Test
    void deleteUser() throws Exception {
        try {
            mockMvc.perform(delete(URL_USER_1));
            fail();
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof RuntimeException);
        }
        mockMvc.perform(get(URL_USER_1)).andExpect(jsonPath("$.username").value("f0rb"));

        mockMvc.perform(delete(URL_USER_2));
        try {
            mockMvc.perform(get(URL_USER_2)).andDo(print()).andExpect(jsonPath("$").doesNotExist());
            fail();
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof EntityNotFoundException);
        }

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

        try {
            mockMvc.perform(get(menuUri + "9"));
            fail();
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof ServiceException);
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
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
               .andExpect(jsonPath("$.updateUserId").value("1"));

        mockMvc.perform(delete(menuUri + "3")).andExpect(statusIs200());

        mockMvc.perform(get(menuUri + "?pageNumber=0"))
               .andExpect(statusIs200())
               .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    public void createMenus() throws Exception {

        requestJson(post("/02/menu/import"), "[{\"menuName\":\"Test Menu1\"},{\"menuName\":\"Test Menu2\"}]", session)
            .andExpect(statusIs200());

        mockMvc.perform(get("/02/menu/"))
               .andDo(print())
               .andExpect(statusIs200())
               .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    public void deleteMenu() throws Exception {
        try {
            mockMvc.perform(delete("/02/menu/0")).andDo(print());
            fail();
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof ServiceException);
        }
    }

    private ResultMatcher statusIs200() {
        return status().is(200);
    }

    /*=============== role ==================*/
    @Test
    public void queryRole() throws Exception {
        mockMvc.perform(get("/role"))
               .andExpect(statusIs200())
               .andExpect(jsonPath("$[0].roleName").value("测试"));
        mockMvc.perform(get("/role?pageNumber=0&pageSize=5"))
               .andExpect(statusIs200())
               .andExpect(jsonPath("$.list[0].roleName").value("测试"))
               .andExpect(jsonPath("$.list[1].roleName").value("高级"))
               .andExpect(jsonPath("$.total").value(2))
        ;
    }

    @Test
    public void deleteRole() {
        try {
            mockMvc.perform(delete("/role/1")).andExpect(statusIs200());
            mockMvc.perform(delete("/role/0"));
            fail();
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof EntityNotFoundException);
        }
    }

    @Test
    public void createRole() throws Exception {
        requestJson(post("/role/"), "{\"roleName\":\"超级\",\"roleCode\":\"VVIP\",\"valid\":true}", session)
            .andExpect(statusIs200());
        mockMvc.perform(get("/role/3"))
               .andExpect(jsonPath("$.roleCode").value("VVIP"))
               .andExpect(jsonPath("$.createUserId").value(1))
               .andExpect(jsonPath("$.updateUserId").value(1));
    }

    @Test
    public void patchAndUpdate() throws Exception {
        requestJson(patch("/role/2"), "{\"roleName\":\"超级\",\"roleCode\":\"VVIP\"}", session)
            .andExpect(statusIs200());
        mockMvc.perform(get("/role/2"))
               .andExpect(jsonPath("$.roleCode").value("VVIP"))
               .andExpect(jsonPath("$.roleName").value("超级"))
               .andExpect(jsonPath("$.valid").value(true))
        ;

        requestJson(put("/role/2"), "{\"roleName\":\"超级\",\"roleCode\":\"VVVIP\"}", session)
            .andExpect(statusIs200());
        mockMvc.perform(get("/role/2"))
               .andExpect(jsonPath("$.roleCode").value("VVVIP"))
               .andExpect(jsonPath("$.roleName").value("超级"))
               .andExpect(jsonPath("$.valid").doesNotExist())
        ;
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

    /*=============== AssociativeService ==================*/
    @Resource
    AssociativeService<Long, Integer> userAndRoleAssociativeService;

    @Test
    void associativeService$count() {
        assertEquals(0L, userAndRoleAssociativeService.count(emptyList(), emptyList()));
    }

    @Test
    void associativeService$exists() {
        assertTrue(userAndRoleAssociativeService.exists(1L, 1));

        assertTrue(userAndRoleAssociativeService.exists(1L, Arrays.asList(1, 2)));
        assertArrayEquals(new Object[]{1, 2}, userAndRoleAssociativeService.getByLeftId(1L).toArray());
        assertArrayEquals(new Object[]{1L, 4L}, userAndRoleAssociativeService.getByRightId(2).toArray());

        userAndRoleAssociativeService.deallocate(1L, 1);
        assertTrue(userAndRoleAssociativeService.exists(1L, Arrays.asList(1, 2)));
        assertFalse(userAndRoleAssociativeService.exists(singleton(1L), 1));

        userAndRoleAssociativeService.allocate(1L, 1);
        assertTrue(userAndRoleAssociativeService.exists(singleton(1L), 1));
    }

    @Test
    void associativeService$reallocate() {
        assertTrue(userAndRoleAssociativeService.exists(1L, 1));

        assertEquals(2, userAndRoleAssociativeService.reallocateForLeft(1L, Arrays.asList(2, 3)));
        assertFalse(userAndRoleAssociativeService.exists(1L, 1));
        assertTrue(userAndRoleAssociativeService.exists(1L, Arrays.asList(1, 2)));
        assertTrue(userAndRoleAssociativeService.exists(1L, Arrays.asList(2, 3)));

        assertEquals(0, userAndRoleAssociativeService.reallocateForRight(2, emptyList()));
        assertEquals(0, userAndRoleAssociativeService.reallocateForRight(3, emptyList()));
        assertFalse(userAndRoleAssociativeService.exists(1L, Arrays.asList(1, 2, 3)));
    }

    /*=============== Join ==================*/
    @Test
    void queryForJoin() {
        TestJoinQuery query = new TestJoinQuery();
        query.setSort("userCount,desc");

        List<UserCountByRoleView> list = new JoinQueryExecutor<>(jdbcOperations, UserCountByRoleView.class).query(query);
        assertThat(list).extracting(UserCountByRoleView::getUserCount).containsExactly(3, 2);
    }

    @Test
    void pageForJoin() {
        TestJoinQuery testJoinQuery = new TestJoinQuery();
        testJoinQuery.setRoleName("高级");
        PageList<TestJoinView> page = new JoinQueryExecutor<>(jdbcOperations, TestJoinView.class).page(testJoinQuery);
        assertThat(page.getTotal()).isEqualTo(2);
        assertThat(page.getList()).extracting(TestJoinView::getUsername).containsExactly("f0rb", "user4");
        assertThat(testJoinQuery.getPageNumber()).isEqualTo(0);
        assertThat(testJoinQuery.getPageSize()).isEqualTo(10);
    }

    /*=============== Cache ==================*/
    @Test
    void defaultNoCache() throws IllegalAccessException {
        RoleController roleController = wac.getBean(RoleController.class);
        CacheWrapper entityCacheWrapper = (CacheWrapper) FieldUtils.readField(roleController, "entityCacheWrapper", true);
        assertThat(entityCacheWrapper.getCache()).isInstanceOf(NoOpCache.class);
    }
}