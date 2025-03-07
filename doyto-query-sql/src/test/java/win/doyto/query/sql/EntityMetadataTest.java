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

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import win.doyto.query.annotation.Entity;
import win.doyto.query.annotation.GroupBy;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.test.menu.MenuEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * EntityMetadataTest
 *
 * @author f0rb on 2022-05-13
 * @since 0.3.1
 */
class EntityMetadataTest {

    @Getter
    @Setter
    @Entity(name = "t_score")
    private static class ScoreGroupByStudentView {
        @GroupBy
        private Long studentId;
        private Double avgScore;
    }

    @Test
    void supportGroupByAnnotationForUnderscoreColumn() {
        EntityMetadata entityMetadata = new EntityMetadata(ScoreGroupByStudentView.class);
        assertEquals("student_id AS studentId, avg(score) AS avgScore", entityMetadata.getColumnsForSelect());
        assertEquals(" GROUP BY student_id", entityMetadata.getGroupBySql());
    }

    @Test
    void supportGroupByAnnotationForCamelCaseColumn() {
        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(false);

        EntityMetadata entityMetadata = new EntityMetadata(ScoreGroupByStudentView.class);
        assertEquals("studentId, avg(score) AS avgScore", entityMetadata.getColumnsForSelect());
        assertEquals(" GROUP BY studentId", entityMetadata.getGroupBySql());

        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(true);
    }

    @Test
    void resolveSelectColumns() {
        String columns = EntityMetadata.buildViewColumns(MenuEntity.class);
        assertEquals("id, parent_id AS parentId, menu_name AS menuName, platform, memo, valid", columns);
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
        assertEquals("max(id)", EntityMetadata.resolveColumn("maxId"));
        assertEquals("min(id)", EntityMetadata.resolveColumn("minId"));
        assertEquals("sum(qty)", EntityMetadata.resolveColumn("sumQty"));
        assertEquals("avg(qty)", EntityMetadata.resolveColumn("avgQty"));
        assertEquals("first(id)", EntityMetadata.resolveColumn("firstId"));
        assertEquals("last(id)", EntityMetadata.resolveColumn("lastId"));
        assertEquals("stddev(sales_amount)", EntityMetadata.resolveColumn("stdDevSalesAmount"));
        assertEquals("stddev_pop(sales_amount)", EntityMetadata.resolveColumn("stdDevPopSalesAmount"));
        assertEquals("stddev_samp(sales_amount)", EntityMetadata.resolveColumn("stdDevSampSalesAmount"));
        assertEquals("addToSet(sales_amount)", EntityMetadata.resolveColumn("addToSetSalesAmount"));
        assertEquals("push(sales_amount)", EntityMetadata.resolveColumn("pushSalesAmount"));
        assertEquals("count(*)", EntityMetadata.resolveColumn("count"));
        assertEquals("count(id)", EntityMetadata.resolveColumn("countId"));
    }
}