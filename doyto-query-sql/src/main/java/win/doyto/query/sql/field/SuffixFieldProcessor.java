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

import win.doyto.query.util.ColumnUtil;

import java.lang.reflect.Field;
import java.util.List;

/**
 * SuffixFieldProcessor
 *
 * @author f0rb on 2023/10/9
 * @since 1.0.3
 */
class SuffixFieldProcessor implements FieldProcessor {

    private String columnName;
    private final SqlQuerySuffix sqlQuerySuffix;

    public SuffixFieldProcessor(Field field) {
        this(field.getName(), false);
    }

    public SuffixFieldProcessor(Field field, boolean isAggregateField) {
        this(field.getName(), isAggregateField);
    }

    SuffixFieldProcessor(String fieldName, boolean isAggregateField) {
        this.sqlQuerySuffix = SqlQuerySuffix.resolve(fieldName);
        this.columnName = this.sqlQuerySuffix.removeSuffix(fieldName);
        if (isAggregateField) {
            columnName = ColumnUtil.resolveColumn(columnName);
        } else {
            columnName = columnName.replace("$", ".");
        }
    }

    @Override
    public String process(String alias, List<Object> argList, Object value) {
        columnName = ColumnUtil.convertColumn(columnName);
        value = sqlQuerySuffix.getValueProcessor().escapeValue(value);
        return sqlQuerySuffix.buildColumnCondition(alias + columnName, argList, value);
    }
}
