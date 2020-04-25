package win.doyto.query.demo.test;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ExceptionTest
 *
 * @author f0rb on 2020-04-01
 */
class ExceptionTest extends DemoApplicationTest {


    private ResultActions performAndExpectFail(RequestBuilder requestBuilder, String expectedMessage) throws Exception {
        return mockMvc.perform(requestBuilder)
                      .andDo(print())
                      .andExpect(status().isOk())
                      .andExpect(jsonPath("$.success").value(false))
                      .andExpect(jsonPath("$.message").value(expectedMessage))
                ;
    }

    @Test
    void testHttpRequestMethodNotSupportedException() throws Exception {
        performAndExpectFail(post("/user/1"), "该接口不支持POST请求");
    }

    @Test
    void entityNotFound() throws Exception {
        performAndExpectFail(get("/user/-1"), "查询记录不存在");
    }

    @Test
    void testMethodArgumentTypeMismatchException() throws Exception {
        performAndExpectFail(get("/user/null"), "参数类型异常");
    }

    @Test
    void testMethodArgumentNotValidException() throws Exception {
        RequestBuilder requestBuilder = post("/user/").content("{}").contentType(MediaType.APPLICATION_JSON);
        performAndExpectFail(requestBuilder, "参数校验失败")
                .andExpect(jsonPath("$.hints[0].username").value("must not be null"))
                .andExpect(jsonPath("$.hints[0].password").value("must not be null"))
        ;
    }

    @Test
    void testMethodArgumentNotValidExceptionWithList() throws Exception {
        RequestBuilder requestBuilder = post("/user/").content("[{\"username\":\"test\"},{\"password\":\"123456\"}]").contentType(MediaType.APPLICATION_JSON);
        performAndExpectFail(requestBuilder, "参数校验失败")
                .andExpect(jsonPath("$.hints[0].password").value("must not be null"))
                .andExpect(jsonPath("$.hints[1].username").value("must not be null"))
        ;

        RequestBuilder postRole = post("/role/").content("{}").contentType(MediaType.APPLICATION_JSON);
        performAndExpectFail(postRole, "参数校验失败")
                .andExpect(jsonPath("$.hints[0].roleName").value("must not be null"))
                .andExpect(jsonPath("$.hints[0].roleCode").value("must not be null"))
        ;
    }

    @Test
    void testConstraintViolationException() throws Exception {
        performAndExpectFail(get("/user/username?username=sa"), "参数校验失败")
                .andExpect(jsonPath("$.hints.username").value("size must be between 4 and 20"));
        performAndExpectSuccess(get("/user/username?username=f0rb"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    void testBindException() throws Exception {
        performAndExpectFail(get("/user/email"), "参数校验失败")
                .andExpect(jsonPath("$.hints.email").value("must not be null"));


        performAndExpectSuccess(get("/user/email?email=test@163.com"));
    }

    @Test
    void testDuplicateKeyException() throws Exception {
        RequestBuilder postUser = post("/user/")
                .content("[{\"username\":\"user2\", \"password\":\"123456\"}]")
                .contentType(MediaType.APPLICATION_JSON);
        performAndExpectFail(postUser, "该数据已存在");
    }
}
