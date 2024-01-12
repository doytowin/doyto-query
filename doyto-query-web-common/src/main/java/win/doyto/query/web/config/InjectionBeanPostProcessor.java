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

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import win.doyto.query.util.CommonUtil;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * InjectionBeanPostProcessor
 *
 * Inject properties for fields already assigned
 * and annotated with {@link InjectBean}
 *
 * @author f0rb on 2021-12-11
 */
@RequiredArgsConstructor
class InjectionBeanPostProcessor implements BeanPostProcessor {

    private final AutowireCapableBeanFactory beanFactory;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        for (Field field : FieldUtils.getFieldsWithAnnotation(bean.getClass(), InjectBean.class)) {
            Optional.ofNullable(CommonUtil.readField(field, bean))
                    .ifPresent(beanFactory::autowireBean);
        }
        return bean;
    }
}
