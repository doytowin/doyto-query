/*
 * Copyright © 2019-2025 DoytoWin, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package win.doyto.query.web.demo.test;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import win.doyto.query.web.response.PresetErrorCode;

import static org.hamcrest.Matchers.containsInRelativeOrder;
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
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.list[0].nickname").value("测试1"))
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
        performAndExpectSuccess(get(path + "?pageSize=1"))
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

        String content = "{\"id\":4,\"username\":\"test4\",\"mobile\":\"166666666\",\"valid\":false}";
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
        RequestBuilder requestBuilder = patch("/user/2").content("{\"id\":2,\"nickname\":\"new name\"}").contentType(MediaType.APPLICATION_JSON);
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

    @Test
    void queryUsersWhoHavePerm5() throws Exception {
        performAndExpectSuccess(get("/user/?perm.id=5"))
                .andExpect(jsonPath("$.data.list[*].id", containsInRelativeOrder(1, 4)));
    }

    @Test
    @Rollback
    void deleteByQuery() throws Exception {
        performAndExpectFail(delete("/user/"), PresetErrorCode.ARGUMENT_VALIDATION_FAILED)
                .andExpect(jsonPath("$.hints.query").value("不能为空"));
        performAndExpectFail(delete("/user/?username=test&sort=anyti;DROP database;"), PresetErrorCode.ARGUMENT_VALIDATION_FAILED)
                .andExpect(jsonPath("$.hints.sort").exists());
        performAndExpectSuccess(delete("/user/?username=f0rb"))
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    @Rollback
    void patchUserByQuery() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = patch("/user/?emailLike=qq.com&valid=true")
                .content("{\"valid\":false}").contentType(MediaType.APPLICATION_JSON);
        performAndExpectSuccess(requestBuilder)
                .andExpect(jsonPath("$.data").value(2));

        requestBuilder = patch("/user/?username=test&sort=anyti;DROP database;")
                .content("{\"valid\":false}").contentType(MediaType.APPLICATION_JSON);
        performAndExpectFail(requestBuilder, PresetErrorCode.ARGUMENT_VALIDATION_FAILED)
                .andExpect(jsonPath("$.hints.sort").exists());
    }

    @Test
    @Rollback
    void patchUserByEmptyQuery() throws Exception {
        RequestBuilder requestBuilder = patch("/user/")
                .content("{\"valid\":false}").contentType(MediaType.APPLICATION_JSON);
        performAndExpectFail(requestBuilder, PresetErrorCode.ARGUMENT_VALIDATION_FAILED)
                .andExpect(jsonPath("$.hints.query").value("不能为空"))
        ;
    }

    @Test
    void queryForColumns() throws Exception {
        performAndExpectSuccess(get("/user/columns/username,email"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[1].username").value("user2"))
                .andExpect(jsonPath("$.data[1].email").value("test2@qq.com"))
                .andExpect(jsonPath("$.data[1].id").doesNotExist())
                .andExpect(jsonPath("$.data[1].mobile").doesNotExist());
    }

    @Test
    void queryForSnakeCaseColumns() throws Exception {
        performAndExpectSuccess(get("/user/columns/username,userLevel"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[1].username").value("user2"))
                .andExpect(jsonPath("$.data[1].userLevel").value("普通"))
                .andExpect(jsonPath("$.data[1].id").doesNotExist())
                .andExpect(jsonPath("$.data[1].mobile").doesNotExist());
    }

    @Test
    void queryCountOfEachLevel() throws Exception {
        performAndExpectSuccess(get("/user/queryCountOfEachLevel?valid=true&countGt=1"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.size()").value(1))
                .andExpect(jsonPath("$.data[0].userLevel").value("普通"))
                .andExpect(jsonPath("$.data[0].count").value(2));
    }
}
