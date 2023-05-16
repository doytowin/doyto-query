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

package win.doyto.query.web;

import jakarta.validation.Configuration;
import jakarta.validation.ParameterNameProvider;
import org.springframework.boot.autoconfigure.validation.ValidationConfigurationCustomizer;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

@org.springframework.context.annotation.Configuration
public class AValidationConfigurationCustomizer implements ValidationConfigurationCustomizer {

    @Override
    public void customize(Configuration<?> configuration) {
        ParameterNameProvider defaultProvider = configuration.getDefaultParameterNameProvider();
        ParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();
        configuration.parameterNameProvider(new ParameterNameProvider() {
            @Override
            public List<String> getParameterNames(Constructor<?> constructor) {
                String[] paramNames = discoverer.getParameterNames(constructor);
                return (paramNames != null ? Arrays.asList(paramNames) :
                        defaultProvider.getParameterNames(constructor));
            }

            @Override
            public List<String> getParameterNames(Method method) {
                String[] paramNames = discoverer.getParameterNames(method);
                return (paramNames != null ? Arrays.asList(paramNames) :
                        defaultProvider.getParameterNames(method));
            }
        });
    }
}
