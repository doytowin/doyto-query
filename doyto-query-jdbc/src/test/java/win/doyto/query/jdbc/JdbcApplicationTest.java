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

package win.doyto.query.jdbc;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.dialect.OracleDialect;

import java.util.Arrays;

/**
 * JdbcApplicationTest
 *
 * @author f0rb on 2021-11-28
 */
@Transactional
@Rollback
@SpringBootTest
abstract class JdbcApplicationTest {

    @BeforeAll
    static void beforeAll(@Autowired Environment environment) {
        if (Arrays.asList(environment.getActiveProfiles()).contains("oracle")) {
            GlobalConfiguration.instance().setDialect(new OracleDialect());
        }
    }
}
