package win.doyto.query.demo.test;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.RequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * UserControllerTest
 *
 * @author f0rb on 2020-04-01
 */
class UserMvcTest extends DemoApplicationTest {

    @Test
    void getById() throws Exception {
        performAndExpectSuccess(get("/user/1"))
                .andExpect(jsonPath("$.data.username").value("f0rb"))
                .andExpect(jsonPath("$.data.nickname").value("测试1"))
        ;
    }

    @Test
    void queryByUsername() throws Exception {
        performAndExpectSuccess(get("/user/?username=f0rb"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].nickname").value("测试1"))
        ;

        performAndExpectSuccess(get("/user/?username=f0rb&pageNumber=2"))
                .andExpect(jsonPath("$.data.list.size()").value(0))
        ;
    }

    @Test
    @Rollback
    void add() throws Exception {
        String content = "{\"username\":\"test5\",\"password\":\"123456\",\"valid\":true}";
        RequestBuilder requestBuilder = post("/user/").content(content).contentType(MediaType.APPLICATION_JSON);
        performAndExpectSuccess(requestBuilder);
        performAndExpectSuccess(get("/user/?pageSize=1"))
                .andExpect(jsonPath("$.data.total").value(5))
        ;
    }

    @Test
    @Rollback
    void batch() throws Exception {
        batch("/user/");
    }

    @Test
    @Rollback
    void batch2() throws Exception {
        batch("/user2/");
    }

    private void batch(String path) throws Exception {
        String content = "[{\"username\":\"test5\",\"password\":\"123456\",\"valid\":true},{\"username\":\"test6\",\"password\":\"123456\",\"valid\":true}]";
        RequestBuilder requestBuilder = post(path).content(content).contentType(MediaType.APPLICATION_JSON);
        performAndExpectSuccess(requestBuilder);
        performAndExpectSuccess(get("/user2/" + "?pageSize=1"))
                .andExpect(jsonPath("$.data.total").value(6))
        ;
    }

    @Test
    @Rollback
    void updateUser() throws Exception {
        performAndExpectSuccess(get("/user/4"))
                .andExpect(jsonPath("$.data.mobile").value("17778888884"))
                .andExpect(jsonPath("$.data.nickname").value("测试4"))
                .andExpect(jsonPath("$.data.valid").value(true));

        String content = "{\"username\":\"test4\",\"mobile\":\"166666666\",\"valid\":false}";
        RequestBuilder requestBuilder = put("/user/4").content(content).contentType(MediaType.APPLICATION_JSON);
        performAndExpectSuccess(requestBuilder);
        performAndExpectSuccess(get("/user/4"))
                .andExpect(jsonPath("$.data.mobile").value("166666666"))
                .andExpect(jsonPath("$.data.nickname").doesNotExist())
                .andExpect(jsonPath("$.data.valid").value(false))
        ;
    }

    @Test
    @Rollback
    void patchUser() throws Exception {
        RequestBuilder requestBuilder = patch("/user/2").content("{\"nickname\":\"new name\"}").contentType(MediaType.APPLICATION_JSON);
        performAndExpectSuccess(requestBuilder);
        performAndExpectSuccess(get("/user/2"))
                .andExpect(jsonPath("$.data.nickname").value("new name"))
        ;
    }

    @Test
    @Rollback
    void deleteById() throws Exception {
        performAndExpectSuccess(delete("/user/1"));
        performAndExpectSuccess(get("/user/?pageSize=1"))
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.list[0].id").value(2))
        ;
    }

}
