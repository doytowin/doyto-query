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

package win.doyto.query.jdbc.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceUtils;
import win.doyto.query.jdbc.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

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
    public DatabaseOperations databaseOperations(TransactionExecutor transactionExecutor) {
        return new JdbcDatabaseOperations(transactionExecutor);
    }

    @Bean
    @ConditionalOnClass(DataSourceUtils.class)
    @ConditionalOnMissingBean(TransactionExecutor.class)
    public TransactionExecutor sessionProvider(DataSource dataSource) {
        return new TransactionExecutor() {
            @Override
            public <T> T withTransaction(TransactionBody<T> sql) {
                Connection connection = DataSourceUtils.getConnection(dataSource);
                try {
                    return sql.execute(connection);
                } catch (SQLException e) {
                    throw new SQLRuntimeException(e);
                } finally {
                    DataSourceUtils.releaseConnection(connection, dataSource);
                }
            }
        };
    }
}