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
                .andExpect(jsonPath("$.errors.username[0]").value("must not be null"))
                .andExpect(jsonPath("$.errors.password[0]").value("must not be null"))
        ;
    }
}
