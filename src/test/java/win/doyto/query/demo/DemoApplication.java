package win.doyto.query.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import win.doyto.query.service.AssociativeService;
import win.doyto.query.service.AssociativeServiceTemplate;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * DemoApplication
 *
 * @author f0rb
 */
@SpringBootApplication
@EnableCaching(proxyTargetClass = true)
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class);
    }

    @Resource
    public void setDataSource(DataSource dataSource) {
        new ResourceDatabasePopulator(new ClassPathResource("import.sql")).execute(dataSource);
    }

    @Bean
    public AssociativeService<Long, Integer> UserAndRoleAssociativeService(JdbcOperations jdbcOperations) {
        return new AssociativeServiceTemplate<>(jdbcOperations, "t_user_and_role", "user_id", "role_id");
    }
}
