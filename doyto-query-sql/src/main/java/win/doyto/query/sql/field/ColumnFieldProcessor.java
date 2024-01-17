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

package win.doyto.query.sql.field;

import javax.persistence.Column;
import java.lang.reflect.Field;
import java.util.List;

/**
 * ColumnFieldProcessor
 * fetch column name from {@link Column#name()}
 * and resolve the suffix
 *
 * @author f0rb on 2023/7/13
 * @since 1.0.2
 */
public class ColumnFieldProcessor implements FieldProcessor {

    private final String columnName;
    private final SqlQuerySuffix sqlQuerySuffix;

    public ColumnFieldProcessor(Field field) {
        Column column = field.getAnnotation(Column.class);
        columnName = column.name();
        sqlQuerySuffix = SqlQuerySuffix.resolve(field.getName());
    }

    @Override
    public String process(String alias, List<Object> argList, Object value) {
        value = sqlQuerySuffix.getValueProcessor().escapeValue(value);
        return sqlQuerySuffix.buildColumnCondition(columnName, argList, value);
    }
}
