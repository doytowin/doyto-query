/*
 * Copyright © 2019-2022 Forb Yuan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package win.doyto.query.web.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockEnvironment;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.Dialect;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ResourceAccessMode.READ;
import static win.doyto.query.web.config.DoytoQueryInitializer.key;

/**
 * DoytoQueryInitializerTest
 *
 * @author f0rb on 2020-01-24
 */
class DoytoQueryInitializerTest {

    @BeforeEach
    void setUp() {
        DoytoQueryInitializer doytoQueryInitializer = new DoytoQueryInitializer();
        ConfigurableApplicationContext context = new GenericApplicationContext() {
            @Override
            protected ConfigurableEnvironment createEnvironment() {
                return new MockEnvironment()
                        .withProperty(key("table-format"), "sys_%s")
                        .withProperty(key("join-table-format"), "t_%s_and_%s");
            }
        };
        doytoQueryInitializer.initialize(context);
    }

    @AfterEach
    void tearDown() {
        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(false);
    }

    @ResourceLock(value = "ignoreCacheException", mode = READ)
    @Test
    void initialize() {
        GlobalConfiguration globalConfiguration = GlobalConfiguration.instance();
        assertFalse(globalConfiguration.isMapCamelCaseToUnderscore());
        assertTrue(globalConfiguration.isIgnoreCacheException());
        assertTrue(globalConfiguration.getDialect() instanceof Dialect);
        assertEquals("sys_%s", globalConfiguration.getTableFormat());
        assertEquals("t_%s_and_%s", globalConfiguration.getJoinTableFormat());
    }

    @Test
    void newDialect() {
        DoytoQueryInitializer doytoQueryInitializer = new DoytoQueryInitializer();
        assertThrows(ClassNotFoundException.class,
                     () -> doytoQueryInitializer.newDialect("fake.DialectClass"));
    }

    /**
     * Since the page number from request NOW starts from 1
     * and the page number processed in backend always starts from 0,
     * so we need to adjust the page number by minus 1,
     * and the result should >= 0.
     */
    @Test
    void testSetPageNumber() {
        GlobalConfiguration globalConfiguration = GlobalConfiguration.instance();
        assertEquals(0, (int) globalConfiguration.getStartPageNumberAdjuster().apply(1));
        assertEquals(0, (int) globalConfiguration.getStartPageNumberAdjuster().apply(0));
        assertEquals(7, (int) globalConfiguration.getStartPageNumberAdjuster().apply(8));
    }

    @Test
    void fixMapCamelCaseToUnderscore() {
        DoytoQueryInitializer doytoQueryInitializer = new DoytoQueryInitializer();
        ConfigurableApplicationContext context = new GenericApplicationContext() {
            @Override
            protected ConfigurableEnvironment createEnvironment() {
                return new MockEnvironment()
                        .withProperty(key("map-camel-case-to-underscore"), "true");
            }
        };
        doytoQueryInitializer.initialize(context);
        assertTrue(GlobalConfiguration.instance().isMapCamelCaseToUnderscore());
    }
}