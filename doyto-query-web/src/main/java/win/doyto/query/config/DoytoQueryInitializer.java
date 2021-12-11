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

        configCamelCase(globalConfiguration, environment);
        configIgnoreCacheException(globalConfiguration, environment);
        configDialect(globalConfiguration, environment);
        configStartPageNumber(globalConfiguration, environment);
    }

    private void configStartPageNumber(GlobalConfiguration globalConfiguration, ConfigurableEnvironment environment) {
        boolean defaultValue = true;
        globalConfiguration.setStartPageNumberFromOne(environment.getProperty(key("start-page-number-from-one"), boolean.class, defaultValue));
    }

    void configCamelCase(GlobalConfiguration globalConfiguration, ConfigurableEnvironment environment) {
        boolean defaultValue = globalConfiguration.isMapCamelCaseToUnderscore();
        globalConfiguration.setMapCamelCaseToUnderscore(environment.getProperty(key("map-camel-case-to-underscore"), boolean.class, defaultValue));
    }

    private void configIgnoreCacheException(GlobalConfiguration globalConfiguration, ConfigurableEnvironment environment) {
        boolean defaultValue = globalConfiguration.isIgnoreCacheException();
        globalConfiguration.setIgnoreCacheException(environment.getProperty(key("ignore-cache-exception"), boolean.class, defaultValue));
    }

    private void configDialect(GlobalConfiguration globalConfiguration, ConfigurableEnvironment environment) {
        String dialectClass = environment.getProperty(key("dialect"));
        if (dialectClass == null) {
            return;
        }
        Dialect dialect = newDialect(dialectClass);
        globalConfiguration.setDialect(dialect);
    }

    @SneakyThrows
    Dialect newDialect(String dialectClass) {
        return (Dialect) Class.forName(dialectClass).getDeclaredConstructor().newInstance();
    }

    private String key(String key) {
        return DOYTO_QUERY_CONFIG + key;
    }
}
