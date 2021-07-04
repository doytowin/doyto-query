package win.doyto.query.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import win.doyto.query.core.Dialect;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DoytoQueryInitializerTest
 *
 * @author f0rb on 2020-01-24
 */
class DoytoQueryInitializerTest {

    @BeforeEach
    void setUp() {
        DoytoQueryInitializer doytoQueryInitializer = new DoytoQueryInitializer();
        ConfigurableApplicationContext context = new AnnotationConfigWebApplicationContext();
        doytoQueryInitializer.initialize(context);
    }

    @Test
    void initialize() {
        GlobalConfiguration globalConfiguration = GlobalConfiguration.instance();
        assertFalse(globalConfiguration.isMapCamelCaseToUnderscore());
        assertTrue(globalConfiguration.isIgnoreCacheException());
        assertTrue(globalConfiguration.getDialect() instanceof Dialect);
    }

    @Test
    void newDialect() {
        DoytoQueryInitializer doytoQueryInitializer = new DoytoQueryInitializer();
        assertThrows(ClassNotFoundException.class,
                     () -> doytoQueryInitializer.newDialect("fake.DialectClass"));
    }

    @Test
    void testSetPageNumber() {
        GlobalConfiguration globalConfiguration = GlobalConfiguration.instance();
        assertEquals(1, (int) globalConfiguration.getStartPageNumberAdjuster().apply(1));
    }
}