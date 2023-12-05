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

package win.doyto.query.sql.field;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SubqueryProcessorTest
 *
 * @author f0rb on 2023/6/13
 * @since 1.0.2
 */
class SubqueryProcessorTest {

    @Test
    void givenCamelCaseWhenBuildShouldBeConvertedToSnakeCase() {
        String clauseFormat = SubqueryProcessor.buildClauseFormat("minScore", "score", "user");
        assertThat(clauseFormat).isEqualTo("min_score = (SELECT score FROM user%s)");
    }

    @Test
    void givenCamelCaseWithAnyWhenBuildShouldBeConvertedToSnakeCase() {
        String clauseFormat = SubqueryProcessor.buildClauseFormat("minScoreGtAny", "score", "user");
        assertThat(clauseFormat).isEqualTo("min_score > ANY(SELECT score FROM user%s)");
    }


    @Test
    void givenCamelCaseWithAllWhenBuildShouldBeConvertedToSnakeCase() {
        String clauseFormat = SubqueryProcessor.buildClauseFormat("minScoreGtAll", "score", "user");
        assertThat(clauseFormat).isEqualTo("min_score > ALL(SELECT score FROM user%s)");
    }

}