package win.doyto.query.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import win.doyto.query.jdbc.DatabaseOperations;
import win.doyto.query.jdbc.DatabaseTemplate;

/**
 * JdbcApplication
 *
 * @author f0rb on 2021-11-28
 */
@SpringBootApplication
@EnableTransactionManagement(proxyTargetClass = true)
public class JdbcApplication {
    public static void main(String[] args) {
        SpringApplication.run(JdbcApplication.class);
    }

    @Bean
    public AssociativeService<Long, Integer> userAndRoleAssociativeService() {
        return new TemplateAssociativeService<>("t_user_and_role", "userId", "roleId", "createUserId");
    }

    @Bean
    public AssociativeService<Integer, Integer> roleAndPermissionAssociativeService() {
        return new TemplateAssociativeService<>("t_role_and_permission", "roleId", "permissionId");
    }

    @Bean
    @ConditionalOnClass({JdbcOperations.class})
    public DatabaseOperations databaseOperations(JdbcOperations jdbcOperations) {
        return new DatabaseTemplate(jdbcOperations);
    }
}
