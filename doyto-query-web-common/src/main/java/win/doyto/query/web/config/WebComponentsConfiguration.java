/*
 * Copyright © 2019-2025 DoytoWin, Inc.
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

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.validation.Validator;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import win.doyto.query.geo.GeoShape;
import win.doyto.query.web.component.ErrorCodeI18nService;

/**
 * WebComponentsConfiguration
 *
 * @author f0rb on 2022-04-06
 */
@Configuration
@ComponentScan("win.doyto.query.web.component")
public class WebComponentsConfiguration {

    @Bean
    public BeanPostProcessor injectionBeanPostProcessor(AutowireCapableBeanFactory beanFactory) {
        return new InjectionBeanPostProcessor(beanFactory);
    }

    @Bean
    public ResourceBundleMessageSource resourceBundleMessageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("business", "error");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }

    @Bean
    public ErrorCodeI18nService errorCodeI18nService() {
        return new ErrorCodeI18nService(resourceBundleMessageSource());
    }

    @Bean
    public ConfigurableWebBindingInitializer configurableWebBindingInitializer(FormattingConversionService conversionService, Validator validator) {
        ConfigurableWebBindingInitializer initializer = new ConfigurableWebBindingInitializer();
        initializer.setConversionService(conversionService);
        initializer.setValidator(validator);
        initializer.setPropertyEditorRegistrar(r -> r.registerCustomEditor(GeoShape.class, new GeoShapeEditor()));
        return initializer;
    }

    @Bean
    public SortFieldsProperties sortFieldsProperties() {
        return new SortFieldsProperties();
    }
}
