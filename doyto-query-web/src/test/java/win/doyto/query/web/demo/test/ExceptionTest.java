package win.doyto.query.web.demo.test;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
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


    private ResultActions performAndExpectFail(String expectedMessage, RequestBuilder requestBuilder) throws Exception {
        return mockMvc.perform(requestBuilder)
                      .andDo(print())
                      .andExpect(status().isOk())
                      .andExpect(jsonPath("$.success").value(false))
                      .andExpect(jsonPath("$.message").value(expectedMessage))
                ;
    }

    @Test
    void testHttpRequestMethodNotSupportedException() throws Exception {
        performAndExpectFail("该接口不支持POST请求", post("/role/1"));
    }

    @Test
    void entityNotFound() throws Exception {
        performAndExpectFail("查询记录不存在", get("/role/-1"));
    }

    @Test
    void testMethodArgumentTypeMismatchException() throws Exception {
        performAndExpectFail("参数类型异常", get("/role/null"));
    }

    @Test
    void testMethodArgumentNotValidException() throws Exception {
        RequestBuilder requestBuilder = post("/role/").content("{}").contentType(MediaType.APPLICATION_JSON);
        performAndExpectFail("参数校验失败", requestBuilder)
                .andExpect(jsonPath("$.hints[0].roleName").value("不能为null"))
                .andExpect(jsonPath("$.hints[0].roleCode").value("不能为null"))
        ;
    }

    @Test
    void testMethodArgumentNotValidExceptionWithList() throws Exception {
        RequestBuilder requestBuilder = post("/role/").content("[{\"roleName\":\"test\"},{\"roleCode\":\"123456\"}]").contentType(MediaType.APPLICATION_JSON);
        performAndExpectFail("参数校验失败", requestBuilder)
                .andExpect(jsonPath("$.hints[0].roleCode").value("不能为null"))
                .andExpect(jsonPath("$.hints[1].roleName").value("不能为null"))
        ;

        RequestBuilder postRole = post("/role/").content("{}").contentType(MediaType.APPLICATION_JSON);
        performAndExpectFail("参数校验失败", postRole)
                .andExpect(jsonPath("$.hints[0].roleName").value("不能为null"))
                .andExpect(jsonPath("$.hints[0].roleCode").value("不能为null"))
        ;
    }

    @Test
    void testConstraintViolationException() throws Exception {
        performAndExpectFail("参数校验失败", get("/role/roleName?roleName=sa"))
                .andExpect(jsonPath("$.hints.roleName").value("个数必须在4和20之间"));
        performAndExpectSuccess(get("/role/roleName?roleName=f0rb"))
                .andExpect(jsonPath("$.data.roleCode").doesNotExist());
    }

    @Test
    void testBindException() throws Exception {
        performAndExpectFail("参数校验失败", get("/role/roleName"))
                .andExpect(jsonPath("$.hints.roleName").value("不能为null"));


        performAndExpectSuccess(get("/role/roleName?roleName=test"));
    }

    @Test
    void testDuplicateKeyException() throws Exception {
        RequestBuilder postRole = post("/role/")
                .content("[{\"roleName\":\"role2\", \"roleCode\":\"123456\"}]")
                .contentType(MediaType.APPLICATION_JSON);
        performAndExpectFail("该数据已存在", postRole);
    }

    @Test
    void resolveLocaleFromCookie() throws Exception {
        performAndExpectFail(
                "Record not found",
                get("/role/-1").cookie(new MockCookie("locale", "en_US"))
        );
    }

    @Test
    void resolveLocaleFromHeader() throws Exception {
        performAndExpectFail(
                "Record not found",
                get("/role/-1").header(HttpHeaders.ACCEPT_LANGUAGE, "en_US")
        );
    }
}
