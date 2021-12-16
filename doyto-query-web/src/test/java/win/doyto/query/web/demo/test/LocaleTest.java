/*
 * Copyright © 2019-2021 Forb Yuan
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
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockCookie;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;

/**
 * LocaleTest
 *
 * @author f0rb on 2021-12-16
 */
class LocaleTest extends DemoApplicationTest {

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

    @Test
    void testCustomResourceBundle() throws Exception {
        performAndExpectFail(
                "Wrong argument type",
                get("/role/null").header(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en")
        );
    }

    @Test
    void testLocaleChangeInterceptor() throws Exception {
        performAndExpectFail("Wrong argument type", get("/role/null?locale=en-US"))
                .andExpect(cookie().value("locale", "en-US"));

        performAndExpectFail("参数类型异常", get("/role/null?locale=zh-CN"))
                .andExpect(cookie().value("locale", "zh-CN"));
    }

}
