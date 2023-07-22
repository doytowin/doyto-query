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

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import win.doyto.query.web.demo.DemoApplication;
import win.doyto.query.web.response.ErrorCode;

import javax.annotation.Resource;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * DemoApplicationTest
 *
 * @author f0rb
 */
@Transactional
@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
abstract class DemoApplicationTest {

    @Resource
    protected MockMvc mockMvc;

    protected ResultActions performAndExpectSuccess(RequestBuilder requestBuilder) throws Exception {
        return performAndExpectOk(requestBuilder)
                .andExpect(jsonPath("$.success").value(true));
    }

    protected ResultActions performAndExpectOk(RequestBuilder requestBuilder) throws Exception {
        return mockMvc.perform(requestBuilder)
                      //.andDo(print())
                      .andExpect(status().isOk());
    }

    protected MockHttpSession session = new MockHttpSession();

    protected ResultActions performAndExpectSuccess(MockHttpServletRequestBuilder builder, String content) throws Exception {
        return performAndExpectSuccess(buildJson(builder, content));
    }

    protected MockHttpServletRequestBuilder buildJson(MockHttpServletRequestBuilder builder, String content) {
        return builder.content(content).contentType("application/json;charset=UTF-8").session(session);
    }

    protected ResultActions performAndExpectFail(RequestBuilder requestBuilder, ErrorCode errorCode) throws Exception {
        return performAndExpectOk(requestBuilder).andExpect(jsonPath("$.code").value(errorCode.getCode()));
    }

}