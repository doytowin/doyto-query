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

package win.doyto.query.sql;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import win.doyto.query.annotation.Entity;
import win.doyto.query.annotation.GroupBy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ResourceAccessMode.READ;

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

    @ResourceLock(value = "mapCamelCaseToUnderscore", mode = READ)
    @Test
    void supportGroupByAnnotation() {
        EntityMetadata entityMetadata = new EntityMetadata(ScoreGroupByStudentView.class);
        assertEquals("student_id AS studentId, avg(score) AS avgScore", entityMetadata.getColumnsForSelect());
        assertEquals(" GROUP BY studentId", entityMetadata.getGroupBySql());
    }
}