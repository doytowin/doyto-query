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

package win.doyto.query.sql.field;

import org.apache.commons.lang3.StringUtils;
import win.doyto.query.core.Query;
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

    private final FieldProcessor fieldProcessor;

    public OrCollectionProcessor(Field field) {
        Class<?> clazz = CommonUtil.resolveActualReturnClass(field);
        if (Query.class.isAssignableFrom(clazz)) {
            fieldProcessor = new ConnectableFieldProcessor(clazz);
        } else {
            String orFieldName = StringUtils.removeEnd(field.getName(), "Or");
            fieldProcessor = new SuffixFieldProcessor(orFieldName, false);
        }
    }

    @Override
    public String process(String alias, List<Object> argList, Object collection) {
        if (!(collection instanceof Collection<?>) || ((Collection<?>) collection).isEmpty()) {
            return null;
        }
        return ((Collection<?>) collection).stream()
                .map(value -> fieldProcessor.process(alias, argList, value))
                .collect(Collectors.joining(OR, OP, CP));
    }
}
