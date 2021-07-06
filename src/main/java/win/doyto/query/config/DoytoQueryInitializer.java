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
        globalConfiguration.setStartPageNumberFromOne(environment.getProperty(getKey("start-page-number-from-one"), boolean.class, false));
    }

    void configCamelCase(GlobalConfiguration globalConfiguration, ConfigurableEnvironment environment) {
        globalConfiguration.setMapCamelCaseToUnderscore(environment.getProperty(getKey("map-camel-case-to-underscore"), boolean.class, globalConfiguration.isMapCamelCaseToUnderscore()));
    }

    private void configIgnoreCacheException(GlobalConfiguration globalConfiguration, ConfigurableEnvironment environment) {
        globalConfiguration.setIgnoreCacheException(environment.getProperty(getKey("ignore-cache-exception"), boolean.class, globalConfiguration.isIgnoreCacheException()));
    }

    private void configDialect(GlobalConfiguration globalConfiguration, ConfigurableEnvironment environment) {
        String dialectClass = environment.getProperty(getKey("dialect"), globalConfiguration.getDialect().getClass().getName());
        Dialect dialect = newDialect(dialectClass);
        globalConfiguration.setDialect(dialect);
    }

    @SneakyThrows
    Dialect newDialect(String dialectClass) {
        return (Dialect) Class.forName(dialectClass).getDeclaredConstructor().newInstance();
    }

    private String getKey(String key) {
        return DOYTO_QUERY_CONFIG + key;
    }
}
