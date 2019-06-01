package win.doyto.query.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * DoytoQueryInitializer
 *
 * @author f0rb
 */
public class DoytoQueryInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final String MAP_CAMEL_CASE_TO_UNDERSCORE = "doyto.query.config.map-camel-case-to-underscore";

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        ConfigurableEnvironment environment = context.getEnvironment();
        String enabled = environment.getProperty(MAP_CAMEL_CASE_TO_UNDERSCORE, "false");
        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(Boolean.valueOf(enabled));
    }
}
