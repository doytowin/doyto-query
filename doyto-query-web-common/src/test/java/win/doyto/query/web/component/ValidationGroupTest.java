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

package win.doyto.query.web.component;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import win.doyto.query.web.DemoApplicationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ValidationGroupTest
 *
 * @author f0rb on 2021-07-02
 */
class ValidationGroupTest extends DemoApplicationTest {

    protected ResultActions requestJson(MockHttpServletRequestBuilder builder, String content, MockHttpSession session) throws Exception {
        return mockMvc.perform(builder.content(content).contentType(MediaType.APPLICATION_JSON).session(session));
    }

    @Test
    void shouldRejectWhenCreateContentContainsId() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = post("/role/");
        requestJson(requestBuilder, "{\"id\":1,\"roleName\":\"超级\",\"roleCode\":\"VVIP\"}", session)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(5))
        ;
    }

    @Test
    void shouldRejectWhenUpdateContentWithoutId() throws Exception {
        requestJson(put("/role/2"), "{\"roleName\":\"超级\",\"roleCode\":\"VVIP\"}", session)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(5))
        ;
    }

    @Test
    void shouldRejectWhenPatchContentWithoutId() throws Exception {
        requestJson(patch("/role/2"), "{\"roleName\":\"超级\",\"roleCode\":\"VVIP\"}", session)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(5))
        ;
    }

}