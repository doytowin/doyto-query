package win.doyto.query.web.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import win.doyto.query.web.WebMvcConfigurerAdapter;

import javax.annotation.Resource;
import javax.sql.DataSource;

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

    @Resource
    public void setDataSource(DataSource dataSource) {
        new ResourceDatabasePopulator(new ClassPathResource("import.sql")).execute(dataSource);
    }
}
