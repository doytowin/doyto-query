/*
 * Copyright © 2019-2024 Forb Yuan
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

package win.doyto.query.web.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import win.doyto.query.web.response.PresetErrorCode;

import java.io.UnsupportedEncodingException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HttpUtilTest
 *
 * @author f0rb on 2020-04-19
 */
class HttpUtilTest {
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void getHeader() {
        request.addHeader(HttpHeaders.ACCEPT, "application/json;charset=UTF-8∂");
        assertTrue(HttpUtil.getHeader(request, HttpHeaders.ACCEPT).startsWith(MediaType.APPLICATION_JSON_VALUE));

        assertNull(HttpUtil.getHeader(request, HttpHeaders.ACCEPT_CHARSET));
    }

    @Test
    void writeJson() throws UnsupportedEncodingException {
        HttpUtil.writeJson(response, PresetErrorCode.INTERNAL_ERROR);
        assertEquals("{\"code\":1,\"message\":\"INTERNAL_ERROR\",\"success\":false}", response.getContentAsString());
    }
}