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

package win.doyto.query.config;

import org.junit.jupiter.api.Test;
import win.doyto.query.core.PageQuery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * GlobalConfigurationTest
 *
 * @author f0rb on 2022-01-10
 */
class GlobalConfigurationTest {

    @Test
    void configPageStartFromOne() {
        PageQuery pageQuery = PageQuery.builder().pageNumber(1).build();

        GlobalConfiguration.instance().setStartPageNumberFromOne(true);
        assertEquals(0, GlobalConfiguration.calcOffset(pageQuery));

        GlobalConfiguration.instance().setStartPageNumberFromOne(false);
        assertEquals(10, GlobalConfiguration.calcOffset(pageQuery));
    }

    @Test
    void tableFormattingShouldIgnoreFormattedTableName() {
        assertThat(GlobalConfiguration.formatTable("user")).isEqualTo("t_user");
        assertThat(GlobalConfiguration.formatTable("t_user")).isEqualTo("t_user");
    }

    @Test
    void tableFormattingShouldConvertTableNameToUnderscoreCase() {
        assertThat(GlobalConfiguration.formatTable("UserDetail")).isEqualTo("t_user_detail");
    }
}