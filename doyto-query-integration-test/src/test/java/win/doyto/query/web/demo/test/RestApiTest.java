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
import win.doyto.query.web.demo.module.user.UserApi;
import win.doyto.query.web.demo.module.user.UserQuery;
import win.doyto.query.web.demo.module.user.UserRequest;
import win.doyto.query.web.demo.module.user.UserResponse;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * RestApiTest
 *
 * @author f0rb on 2021-07-17
 */
class RestApiTest extends DemoApplicationTest {

    @Resource
    UserApi userApi;

    @Test
    void create() throws Exception {
        UserRequest request = new UserRequest();
        request.setUsername("test");
        request.setPassword("test");
        userApi.create(request);
        performAndExpectSuccess(get("/user/"))
                .andExpect(jsonPath("$.data.total").value(5))
        ;
    }

    @Test
    void query() {
        List<UserResponse> userResponses = userApi.query(UserQuery.builder().build());
        assertEquals(4, userResponses.size());
    }
}
