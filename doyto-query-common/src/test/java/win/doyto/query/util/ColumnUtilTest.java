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

package win.doyto.query.util;

import org.junit.jupiter.api.Test;
import win.doyto.query.core.PageQuery;
import win.doyto.query.util.test.TestChildQuery;
import win.doyto.query.util.test.TestQuery;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ColumnUtilTest
 *
 * @author f0rb on 2021-11-21
 */
class ColumnUtilTest {

    @Test
    void testIsSingleColumn() {
        assertTrue(ColumnUtil.isSingleColumn("col"));
        assertFalse(ColumnUtil.isSingleColumn("col1, col2"));
        assertFalse(ColumnUtil.isSingleColumn("col1", "col2", "col3"));
    }

    @Test
    void initFieldsShouldIgnoreFieldsInPageQuery() {
        assertEquals(0, ColumnUtil.initFields(PageQuery.class).length);
    }

    @Test
    void initFieldsSupportHierarchy() {
        Field[] subFields = ColumnUtil.initFields(TestChildQuery.class);
        Field[] parentFields = ColumnUtil.initFields(TestQuery.class);
        assertEquals(1, subFields.length - parentFields.length);
    }

    @Test
    void shouldPlaceFieldsOfSuperClassAtFirstPosition() {
        List<Field> fields = ColumnUtil.filterFields(TestChildQuery.class, ColumnUtil::shouldRetain).toList();
        assertEquals("perm", fields.get(0).getName());
        assertEquals("idIn", fields.get(1).getName());
        assertEquals("oneMoreField", fields.get(fields.size() - 1).getName());
    }
}