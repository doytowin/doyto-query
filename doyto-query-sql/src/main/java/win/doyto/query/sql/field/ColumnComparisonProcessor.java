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

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.List;

/**
 * ColumnComparisonProcessor
 *
 * @author f0rb on 2023/2/18
 * @since 1.0.1
 */
public class ColumnComparisonProcessor implements FieldProcessor {

    private final String clause;

    static boolean support(Field field) {
        return boolean.class.isAssignableFrom(field.getType())
                && SqlComparator.matches(field.getName()).find();
    }

    public ColumnComparisonProcessor(String fieldName) {
        clause = SqlComparator.buildClause(fieldName);
    }

    @Override
    public String process(String alias, List<Object> argList, Object value) {
        return Boolean.TRUE.equals(value) ? StringUtils.replace(clause, "alias.", alias) : null;
    }
}
