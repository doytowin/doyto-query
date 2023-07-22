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

package win.doyto.query.jdbc.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcOperations;
import win.doyto.query.jdbc.DatabaseOperations;
import win.doyto.query.jdbc.DatabaseTemplate;

/**
 * DatabaseOperationsConfiguration
 *
 * @author f0rb on 2022/12/25
 * @since 1.0.1
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean(DatabaseOperations.class)
class DatabaseOperationsConfiguration {
    @Bean
    @Primary
    public DatabaseOperations databaseOperations(JdbcOperations jdbcOperations) {
        return new DatabaseTemplate(jdbcOperations);
    }
}