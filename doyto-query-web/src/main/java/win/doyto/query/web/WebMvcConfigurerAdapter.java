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

package win.doyto.query.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import win.doyto.query.geo.GeoShape;
import win.doyto.query.geo.GeoShapeDeserializer;
import win.doyto.query.geo.Point;
import win.doyto.query.geo.PointDeserializer;
import win.doyto.query.web.component.InjectionBeanPostProcessor;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.http.HttpServletRequest;

/**
 * WebMvcConfigurerAdapter
 *
 * @author f0rb
 */
@ComponentScan("win.doyto.query.web.component")
public abstract class WebMvcConfigurerAdapter implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/favicon.ico");
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 移除重复的HttpMessageConverter
        Set<String> retain = new HashSet<>();
        List<HttpMessageConverter<?>> backup = new LinkedList<>();
        for (HttpMessageConverter<?> converter : converters) {
            String className = converter.getClass().getName();
            if (!retain.contains(className)) {
                retain.add(className);
                if (converter instanceof MappingJackson2HttpMessageConverter) {
                    backup.add(0, converter);
                    configMappingJackson2HttpMessageConverter((MappingJackson2HttpMessageConverter) converter);
                } else {
                    backup.add(converter);
                }
            }
        }
        converters.clear();
        converters.addAll(backup);
    }

    protected void configMappingJackson2HttpMessageConverter(MappingJackson2HttpMessageConverter converter) {
        configMediaTypes(converter);
        configCharset(converter);
        configObjectMapper(converter.getObjectMapper());
    }

    protected void configMediaTypes(MappingJackson2HttpMessageConverter converter) {
        List<MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        supportedMediaTypes.add(MediaType.valueOf("application/*+json"));
        supportedMediaTypes.add(MediaType.TEXT_HTML);
        converter.setSupportedMediaTypes(supportedMediaTypes);
    }

    protected void configCharset(MappingJackson2HttpMessageConverter converter) {
        converter.setDefaultCharset(StandardCharsets.UTF_8);
    }

    public static ObjectMapper configObjectMapper(ObjectMapper objectMapper) {
        return objectMapper
                .enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES)
                .enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
                .enable(JsonParser.Feature.IGNORE_UNDEFINED)
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .setTimeZone(TimeZone.getTimeZone("GMT+8")) // 中国的东8时区
                .registerModule(buildGeoModule())
                .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    @SuppressWarnings("unchecked")
    protected static SimpleModule buildGeoModule() {
        SimpleModule geoModule = new SimpleModule("Geo", new Version(1, 0, 0, "", "win.doyto", "doyto-query-geo"));
        geoModule.addDeserializer(Point.class, new PointDeserializer());
        geoModule.addDeserializer(GeoShape.class, new GeoShapeDeserializer());
        return geoModule;
    }

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
    public LocaleResolver localeResolver(){
        CookieLocaleResolver cookieLocaleResolver = new CookieLocaleResolver() {
            @Override
            protected Locale determineDefaultLocale(HttpServletRequest request) {
                String acceptLanguage = request.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
                if (StringUtils.isBlank(acceptLanguage)) {
                    return super.determineDefaultLocale(request);
                }
                return request.getLocale();
            }
        };
        cookieLocaleResolver.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
        cookieLocaleResolver.setCookieName("locale");
        return cookieLocaleResolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        return new LocaleChangeInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

}