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

package win.doyto.query.sql.field;

import win.doyto.query.util.CommonUtil;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static win.doyto.query.sql.Constant.*;

/**
 * OrCollectionProcessor
 * <p>
 * Support field whose type is Collection and name ends with Or.
 * E.g., {@code List<LineItemFilter> lineItemOr}.
 *
 * @author f0rb on 2023/7/10
 * @since 1.0.2
 */
class OrCollectionProcessor implements FieldProcessor {

    private final ConnectableFieldProcessor fieldProcessor;

    public OrCollectionProcessor(Field field) {
        Class<?> clazz = CommonUtil.resolveActualReturnClass(field);
        fieldProcessor = new ConnectableFieldProcessor(clazz, AND);
    }

    static boolean support(Field field) {
        return Collection.class.isAssignableFrom(field.getType()) && field.getName().endsWith("Or");
    }

    @Override
    public String process(String alias, List<Object> argList, Object collection) {
        return ((Collection<?>) collection).stream()
                .map(value -> fieldProcessor.process(alias, argList, value))
                .collect(Collectors.joining(OR, OP, CP));
    }
}
