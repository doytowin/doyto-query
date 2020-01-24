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
    private static final String MAP_CAMEL_CASE_TO_UNDERSCORE = DOYTO_QUERY_CONFIG + "map-camel-case-to-underscore";
    private static final String DIALECT = DOYTO_QUERY_CONFIG + "dialect";

    @Override
    @SneakyThrows
    public void initialize(ConfigurableApplicationContext context) {
        ConfigurableEnvironment environment = context.getEnvironment();
        String enabled = environment.getProperty(MAP_CAMEL_CASE_TO_UNDERSCORE, "false");
        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(Boolean.valueOf(enabled));

        String ignoreCacheException = environment.getProperty(DOYTO_QUERY_CONFIG + "ignore-cache-exception", "false");
        GlobalConfiguration.instance().setIgnoreCacheException(Boolean.valueOf(ignoreCacheException));

        String splitOrFirst = environment.getProperty(DOYTO_QUERY_CONFIG + "split-or-first", "true");
        GlobalConfiguration.instance().setSplitOrFirst(Boolean.valueOf(splitOrFirst));

        String dialectClass = environment.getProperty(DIALECT, "win.doyto.query.config.MySQLDialect");
        final Dialect dialect = (Dialect) Class.forName(dialectClass).getDeclaredConstructor().newInstance();
        GlobalConfiguration.instance().setDialect(dialect);
    }
}
