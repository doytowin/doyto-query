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

import win.doyto.query.util.CommonUtil;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static win.doyto.query.sql.Constant.*;

/**
 * OrFieldProcessor
 *
 * @author f0rb on 2023/7/10
 * @since 1.0.2
 */
class OrFieldProcessor implements FieldProcessor {
    private final List<FieldProcessor> fieldProcessors;

    public OrFieldProcessor(Field field) {
        String[] fieldNames = CommonUtil.splitByOr(field.getName());
        this.fieldProcessors = Arrays.stream(fieldNames)
                                     .map(name -> new SuffixFieldProcessor(name, true))
                                     .collect(Collectors.toList());

    }

    static boolean support(String fieldName) {
        return CommonUtil.containsOr(fieldName);
    }

    @Override
    public String process(String alias, List<Object> argList, Object value) {
        return fieldProcessors.stream()
                              .map(p -> p.process(alias, argList, value))
                              .collect(Collectors.joining(OR, OP, CP));
    }
}
