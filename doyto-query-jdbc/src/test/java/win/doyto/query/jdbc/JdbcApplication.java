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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.AssociationService;
import win.doyto.query.entity.UserIdProvider;

/**
 * JdbcApplication
 *
 * @author f0rb on 2021-11-28
 */
@SpringBootApplication
@EnableTransactionManagement(proxyTargetClass = true)
public class JdbcApplication {
    static {
        GlobalConfiguration.registerJoinTable("role", "user", "a_user_and_role");
        GlobalConfiguration.registerJoinTable("perm", "role", "a_role_and_perm");
    }

    public static void main(String[] args) {
        SpringApplication.run(JdbcApplication.class);
    }

    @Bean
    public AssociationService<Integer, Integer> roleAndPermissionAssociativeService() {
        return new JdbcAssociationService<>("role", "perm") {};
    }

    @Bean
    public AssociationService<Long, Integer> userAndRoleAssociationService() {
        return new JdbcAssociationService<>("user", "role", "create_user_id") {};
    }

    @Bean
    public UserIdProvider<Integer> userIdProvider() {
        return () -> 0;
    }

}
