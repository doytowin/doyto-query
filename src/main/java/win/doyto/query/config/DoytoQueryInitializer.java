package win.doyto.query.config;

import lombok.SneakyThrows;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import win.doyto.query.core.Dialect;

/**
 * DoytoQueryInitializer
 *
 * @author f0rb
 */
public class DoytoQueryInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final String DOYTO_QUERY_CONFIG = "doyto.query.config.";

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        GlobalConfiguration globalConfiguration = GlobalConfiguration.instance();
        ConfigurableEnvironment environment = context.getEnvironment();

        globalConfiguration.setMapCamelCaseToUnderscore(environment.getProperty(DOYTO_QUERY_CONFIG + "map-camel-case-to-underscore", boolean.class, globalConfiguration.isMapCamelCaseToUnderscore()));

        globalConfiguration.setIgnoreCacheException(environment.getProperty(DOYTO_QUERY_CONFIG + "ignore-cache-exception", boolean.class, globalConfiguration.isIgnoreCacheException()));

        String dialectClass = environment.getProperty(DOYTO_QUERY_CONFIG + "dialect", globalConfiguration.getDialect().getClass().getName());
        Dialect dialect = newDialect(dialectClass);
        globalConfiguration.setDialect(dialect);
    }

    @SneakyThrows
    Dialect newDialect(String dialectClass) {
        return (Dialect) Class.forName(dialectClass).getDeclaredConstructor().newInstance();
    }
}
