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

import org.junit.jupiter.api.Test;
import win.doyto.query.test.PermissionQuery;
import win.doyto.query.test.UserQuery;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DomainPathProcessorTest {
    List<Object> argList = new ArrayList<>();

    @Test
    void supportNestedQueryWithThreeDomainsAndConditions() throws NoSuchFieldException {
        DomainPathProcessor domainPathProcessor = new DomainPathProcessor(UserQuery.class.getDeclaredField("perm"));

        String sql = domainPathProcessor.process(argList, PermissionQuery.builder().valid(true).build());

        String expected = "id IN (SELECT user_id FROM j_user_and_role WHERE role_id IN (" +
                "SELECT role_id FROM j_role_and_perm WHERE perm_id IN (" +
                "SELECT id FROM t_perm WHERE valid = ?" +
                ")))";
        assertThat(sql).isEqualTo(expected);
        assertThat(argList).containsExactly(true);
    }
}