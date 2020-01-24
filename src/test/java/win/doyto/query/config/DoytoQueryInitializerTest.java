package win.doyto.query.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import win.doyto.query.core.Dialect;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DoytoQueryInitializerTest
 *
 * @author f0rb on 2020-01-24
 */
class DoytoQueryInitializerTest {

    @Test
    void initialize() {
        DoytoQueryInitializer doytoQueryInitializer = new DoytoQueryInitializer();
        ConfigurableApplicationContext context = new AnnotationConfigWebApplicationContext();
        doytoQueryInitializer.initialize(context);

        GlobalConfiguration globalConfiguration = GlobalConfiguration.instance();
        assertFalse(globalConfiguration.isMapCamelCaseToUnderscore());
        assertTrue(globalConfiguration.isIgnoreCacheException());
        assertTrue(globalConfiguration.isSplitOrFirst());
        assertTrue(globalConfiguration.getDialect() instanceof Dialect);
    }
}