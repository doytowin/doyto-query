package win.doyto.query;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * DemoApplication
 *
 * @author f0rb
 * @date 2019-05-12
 */
@SpringBootApplication
@EnableCaching(proxyTargetClass = true)
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class);
    }
}
