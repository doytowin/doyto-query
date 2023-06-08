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

package win.doyto.query.jdbc;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseTemplateTest {

    @Test
    void shouldResolveForLongWhenKeyIsBigDecimal() {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        keyHolder.getKeyList().add(Collections.singletonMap("Key", new BigDecimal(20)));

        Long key = DatabaseTemplate.resolveKey(Long.class, keyHolder);

        assertEquals(20L, key);
    }
}