package win.doyto.query.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * WebMvcConfigurerAdapter
 *
 * @author f0rb
 */
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
        List<MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        supportedMediaTypes.add(MediaType.valueOf("application/*+json"));
        supportedMediaTypes.add(MediaType.TEXT_HTML);
        converter.setSupportedMediaTypes(supportedMediaTypes);

        configObjectMapper(converter.getObjectMapper());
    }

    public static ObjectMapper configObjectMapper(ObjectMapper objectMapper) {
        return objectMapper
                .enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES)
                .enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
                .enable(JsonParser.Feature.IGNORE_UNDEFINED)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .setTimeZone(TimeZone.getTimeZone("GMT+8")) // 中国的东8时区
                .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

}