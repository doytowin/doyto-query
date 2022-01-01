package win.doyto.query.jdbc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import win.doyto.query.entity.UserIdProvider;
import win.doyto.query.service.AssociationService;
import win.doyto.query.service.AssociativeService;

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
        return new JdbcAssociativeService<>("t_user_and_role", "userId", "roleId", "createUserId");
    }

    @Bean
    public AssociativeService<Integer, Integer> roleAndPermissionAssociativeService() {
        return new JdbcAssociativeService<>("t_role_and_permission", "roleId", "permissionId");
    }

    @Bean
    public AssociationService<Long, Integer> userAndRoleAssociationService() {
        return new JdbcAssociationService<>("t_user_and_role", "userId", "roleId", "createUserId");
    }

    @Bean
    public UserIdProvider<Integer> userIdProvider() {
        return () -> 0;
    }

}
