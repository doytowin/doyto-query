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

import static win.doyto.query.sql.Constant.AND;

/**
 * CollectionProcessor
 *
 * @author f0rb on 2024/11/21
 */
public class CollectionProcessor implements FieldProcessor {

    private final FieldProcessor fieldProcessor;

    public CollectionProcessor(Field field) {
        Class<?> clazz = CommonUtil.resolveActualReturnClass(field);
        if (Query.class.isAssignableFrom(clazz)) {
            fieldProcessor = new ConnectableFieldProcessor(clazz, AND);
        } else {
            String orFieldName = StringUtils.removeEnd(field.getName(), "And");
            fieldProcessor = new SuffixFieldProcessor(orFieldName, false);
        }
    }

    @Override
    public String process(String alias, List<Object> argList, Object collection) {
        if (!(collection instanceof Collection<?> values) || values.isEmpty()) {
            return null;
        }
        return values.stream().map(value -> fieldProcessor.process(alias, argList, value))
                     .collect(Collectors.joining(AND));
    }
}
