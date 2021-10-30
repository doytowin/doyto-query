package win.doyto.query.web.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * WebMvcConfigurerAdapter
 *
 * @author f0rb
 */
@SuppressWarnings("java:S1610")
@ComponentScan("win.doyto.query.web.component")
public abstract class WebMvcConfigurerAdapter implements WebFluxConfigurer {

}