/*
 * Copyright © 2019-2023 Forb Yuan
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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * LoginTest
 *
 * @author f0rb on 2020-04-11
 */
class LoginMvcTest extends DemoApplicationTest {
    /*=============== login ==================*/
    @Test
    void login() throws Exception {
        String content = "{\"account\":\"f0rb\",\"password\":\"123456\"}";
        MvcResult mvcResult = performAndExpectSuccess(post("/login"), content).andReturn();
        MockHttpSession session = (MockHttpSession) mvcResult.getRequest().getSession();
        mockMvc.perform(get("/account").session(session))
               .andExpect(jsonPath("$.data.id").value("1"))
               .andExpect(jsonPath("$.data.nickname").value("测试1"))
        ;
    }
}
