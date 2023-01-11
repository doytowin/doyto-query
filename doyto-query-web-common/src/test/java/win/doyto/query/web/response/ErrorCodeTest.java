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

package win.doyto.query.web.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ErrorCodeTest
 *
 * @author f0rb on 2020-04-01
 */
class ErrorCodeTest {

    private ErrorCode failCode = ErrorCode.build("fail");

    @Test
    void testAssertTrue() {
        ErrorCode.assertTrue(true, failCode);

        try {
            ErrorCode.assertTrue(false, failCode);
            fail();
        } catch (ErrorCodeException e) {
            assertFalse(e.getErrorCode().isSuccess());
            assertEquals(-1, ErrorCode.build(e.getErrorCode()).getCode());
        }
    }

    @Test
    void assertNotNull() {
        ErrorCode.assertNotNull("ok", failCode);

        try {
            ErrorCode.assertNotNull(null, failCode);
            fail();
        } catch (ErrorCodeException e) {
            assertFalse(e.getErrorCode().isSuccess());
        }
    }
}