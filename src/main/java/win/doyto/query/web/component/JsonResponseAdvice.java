package win.doyto.query.web.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import win.doyto.query.web.response.ErrorCode;
import win.doyto.query.web.response.JsonBody;
import win.doyto.query.web.response.JsonResponse;

import java.lang.reflect.Method;

/**
 * RestResponseAdvice
 *
 * @author f0rb on 2017-01-15.
 */
@Slf4j
@ControllerAdvice
class JsonResponseAdvice implements ResponseBodyAdvice<Object> {
    private static ErrorCode wrap(Object body) {
        if (body instanceof JsonResponse) {
            return (JsonResponse<?>) body;
        } else {
            return ErrorCode.build(body);
        }
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {

        Method method = returnType.getMethod();
        Class<?> declaringClass = method.getDeclaringClass();
        if (AbstractJackson2HttpMessageConverter.class.isAssignableFrom(converterType)
                && (declaringClass.isAnnotationPresent(JsonBody.class) || method.isAnnotationPresent(JsonBody.class))) {
            log.debug("JsonResponse: {}.{}", declaringClass.getName(), method.getName());
            return true;
        }

        return false;
    }

    @Override
    public Object beforeBodyWrite(
            Object body, MethodParameter returnType, MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request, ServerHttpResponse response
    ) {
        return wrap(body);
    }
}
