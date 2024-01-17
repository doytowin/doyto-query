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

package win.doyto.query.jdbc.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcOperations;

/**
 * DataQueryClientAutoConfiguration
 *
 * @author f0rb on 2022/12/25
 * @since 1.0.1
 */
@AutoConfiguration(after = JdbcTemplateAutoConfiguration.class)
@ConditionalOnSingleCandidate(JdbcOperations.class)
@Import({DatabaseOperationsConfiguration.class, DataQueryClientConfiguration.class})
public class DataQueryClientAutoConfiguration {
}