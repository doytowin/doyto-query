package win.doyto.query.web.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import win.doyto.query.web.config.WebFluxConfigurerAdapter;

/**
 * WebFluxApplication
 *
 * @author f0rb on 2021-10-30
 */
@SpringBootApplication
public class WebFluxApplication extends WebFluxConfigurerAdapter {
    public static void main(String[] args) {
        SpringApplication.run(WebFluxApplication.class);
    }
}