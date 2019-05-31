package win.doyto.query.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

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
}
