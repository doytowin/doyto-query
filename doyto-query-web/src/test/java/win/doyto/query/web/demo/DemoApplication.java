package win.doyto.query.web.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import win.doyto.query.jdbc.DatabaseOperations;
import win.doyto.query.jdbc.DatabaseTemplate;
import win.doyto.query.web.WebMvcConfigurerAdapter;

/**
 * DemoApplication
 *
 * @author f0rb
 */
@SpringBootApplication
@EnableCaching(proxyTargetClass = true)
@EnableTransactionManagement(proxyTargetClass = true)
public class DemoApplication extends WebMvcConfigurerAdapter {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class);
    }

    @Bean
    @ConditionalOnClass({JdbcOperations.class})
    public DatabaseOperations databaseOperations(JdbcOperations jdbcOperations) {
        return new DatabaseTemplate(jdbcOperations);
    }
}
