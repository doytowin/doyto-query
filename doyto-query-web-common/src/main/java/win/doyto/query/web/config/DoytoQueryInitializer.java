/*
 * Copyright © 2019-2024 Forb Yuan
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

import lombok.SneakyThrows;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import win.doyto.query.config.GlobalConfiguration;
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
        configJoinTableFormat(globalConfiguration, environment);
        configTableFormat(globalConfiguration, environment);
        configJoinIdFormat(globalConfiguration, environment);
    }

    private void configJoinIdFormat(GlobalConfiguration globalConfiguration, ConfigurableEnvironment environment) {
        String defaultValue = globalConfiguration.getJoinIdFormat();
        globalConfiguration.setJoinIdFormat(environment.getProperty(key("join-id-format"), String.class, defaultValue));
    }

    private void configTableFormat(GlobalConfiguration globalConfiguration, ConfigurableEnvironment environment) {
        String defaultValue = globalConfiguration.getTableFormat();
        globalConfiguration.setTableFormat(environment.getProperty(key("table-format"), String.class, defaultValue));
    }

    private void configJoinTableFormat(GlobalConfiguration globalConfiguration, ConfigurableEnvironment environment) {
        String defaultValue = globalConfiguration.getJoinTableFormat();
        globalConfiguration.setJoinTableFormat(environment.getProperty(key("join-table-format"), String.class, defaultValue));
    }

    private void configStartPageNumber(GlobalConfiguration globalConfiguration, ConfigurableEnvironment environment) {
        boolean defaultValue = true;
        globalConfiguration.setStartPageNumberFromOne(environment.getProperty(key("start-page-number-from-one"), boolean.class, defaultValue));
    }

    private void configCamelCase(GlobalConfiguration globalConfiguration, ConfigurableEnvironment environment) {
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

    static String key(String key) {
        return DOYTO_QUERY_CONFIG + key;
    }
}
