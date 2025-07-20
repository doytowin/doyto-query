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

package win.doyto.query.sql;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * OrderByBuilderTest
 *
 * @author f0rb on 2020-01-01
 */
class OrderByBuilderTest {
    @Test
    void build() {
        assertEquals("valid,asc;id,desc", OrderByBuilder.create().asc("valid").desc("id").toString());
        assertEquals("field(gender,'male','female');id,desc",
                     OrderByBuilder.create().field("gender", "'male'", "'female'").desc("id").toString());
    }
}