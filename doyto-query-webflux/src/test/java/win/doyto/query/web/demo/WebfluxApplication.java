package win.doyto.query.web.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import win.doyto.query.web.config.WebMvcConfigurerAdapter;

/**
 * WebfluxApplication
 *
 * @author f0rb on 2021-10-30
 */
@SpringBootApplication
public class WebfluxApplication extends WebMvcConfigurerAdapter {
    public static void main(String[] args) {
        SpringApplication.run(WebfluxApplication.class);
    }
}