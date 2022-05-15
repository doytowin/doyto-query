/*
 * Copyright © 2019-2022 Forb Yuan
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

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.PageQuery;
import win.doyto.query.test.TestChildQuery;
import win.doyto.query.test.TestEntity;
import win.doyto.query.test.TestQuery;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ResourceAccessMode.READ_WRITE;

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

    @ResourceLock(value = "mapCamelCaseToUnderscore", mode = READ_WRITE)
    @Test
    void resolveSelectColumns() {
        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(true);
        String columns = StringUtils.join(ColumnUtil.resolveSelectColumns(TestEntity.class), ", ");
        assertEquals("username, password, mobile, email, nickname, user_level AS userLevel, memo, valid, id", columns);
        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(false);
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
        List<Field> fields = ColumnUtil.filterFields(TestChildQuery.class, ColumnUtil::shouldRetain).collect(Collectors.toList());
        assertEquals("perm", fields.get(0).getName());
        assertEquals("idIn", fields.get(1).getName());
        assertEquals("oneMoreField", fields.get(fields.size() - 1).getName());
    }

    /**
     * Aggregate function list
     * <p>
     * sum
     * max
     * min
     * avg
     * first
     * last
     * stdDev("stddev")
     * stdDevPop("stddev_pop")
     * stdDevSamp("stddev_samp")
     * addToSet
     * push
     */
    @Test
    void supportAggregateColumnResolving() {
        assertEquals("max(id)", ColumnUtil.resolveColumn("maxId"));
        assertEquals("min(id)", ColumnUtil.resolveColumn("minId"));
        assertEquals("sum(qty)", ColumnUtil.resolveColumn("sumQty"));
        assertEquals("avg(qty)", ColumnUtil.resolveColumn("avgQty"));
        assertEquals("first(id)", ColumnUtil.resolveColumn("firstId"));
        assertEquals("last(id)", ColumnUtil.resolveColumn("lastId"));
        assertEquals("stddev(salesAmount)", ColumnUtil.resolveColumn("stdDevSalesAmount"));
        assertEquals("stddev_pop(salesAmount)", ColumnUtil.resolveColumn("stdDevPopSalesAmount"));
        assertEquals("stddev_samp(salesAmount)", ColumnUtil.resolveColumn("stdDevSampSalesAmount"));
        assertEquals("addToSet(salesAmount)", ColumnUtil.resolveColumn("addToSetSalesAmount"));
        assertEquals("push(salesAmount)", ColumnUtil.resolveColumn("pushSalesAmount"));
    }
}