/*
 * Copyright © 2019-2023 Forb Yuan
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

        if (AbstractJackson2HttpMessageConverter.class.isAssignableFrom(converterType)
                && isAnnotatedByJsonBody(returnType)) {
            log.debug("JsonResponse: {}.{}", returnType.getDeclaringClass().getName(), returnType.getExecutable().getName());
            return true;
        }

        return false;
    }

    private boolean isAnnotatedByJsonBody(MethodParameter returnType) {
        return returnType.getDeclaringClass().isAnnotationPresent(JsonBody.class)
                || returnType.hasMethodAnnotation(JsonBody.class);
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
