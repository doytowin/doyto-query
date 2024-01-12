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

package win.doyto.query.web.component;

import org.junit.jupiter.api.Test;
import win.doyto.query.test.TestQuery;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NotEmptyQueryValidatorTest
 *
 * @author f0rb on 2023/2/25
 * @since 1.0.1
 */
class NotEmptyQueryValidatorTest {

    private NotEmptyQueryValidator notEmptyQueryValidator = new NotEmptyQueryValidator();

    @Test
    void shouldNotBeValidWhenQueryObjectHasNoParameters() {
        assertFalse(notEmptyQueryValidator.isValid(new TestQuery(), null));
    }

    @Test
    void shouldBeValidWhenQueryWithPage() {
        assertTrue(notEmptyQueryValidator.isValid(TestQuery.builder().pageNumber(1).build(), null));
        assertTrue(notEmptyQueryValidator.isValid(TestQuery.builder().pageSize(5).build(), null));
    }

    @Test
    void shouldBeValidWhenQueryWithAnyParameters() {
        assertTrue(notEmptyQueryValidator.isValid(TestQuery.builder().id(10).build(), null));
        assertTrue(notEmptyQueryValidator.isValid(TestQuery.builder().memoNotNull(true).build(), null));

        assertFalse(notEmptyQueryValidator.isValid(TestQuery.builder().idNotIn(Arrays.asList()).build(), null));
        assertTrue(notEmptyQueryValidator.isValid(TestQuery.builder().idNotIn(Arrays.asList(1, 2, 3)).build(), null));
        assertTrue(notEmptyQueryValidator.isValid(TestQuery.builder().idIn(Arrays.asList()).build(), null));
    }

}