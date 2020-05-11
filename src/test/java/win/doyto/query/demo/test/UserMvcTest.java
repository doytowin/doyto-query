package win.doyto.query.demo.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import win.doyto.query.demo.module.user.TestUserEntityAspect;

import javax.annotation.Resource;
import javax.persistence.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserMvcTest
 *
 * @author f0rb on 2020-04-11
 */
public class UserMvcTest extends DemoApplicationTest {

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

}
