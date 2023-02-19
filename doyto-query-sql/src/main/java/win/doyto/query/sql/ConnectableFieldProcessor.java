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

package win.doyto.query.sql;

import org.apache.commons.lang3.StringUtils;
import win.doyto.query.core.QuerySuffix;
import win.doyto.query.util.ColumnUtil;
import win.doyto.query.util.CommonUtil;

import java.lang.reflect.Field;
import java.util.List;
import java.util.StringJoiner;

import static win.doyto.query.sql.Constant.CP;
import static win.doyto.query.sql.Constant.OP;
import static win.doyto.query.sql.FieldProcessor.execute;

/**
 * ConnectableFieldProcessor
 *
 * @author f0rb on 2023/2/19
 * @since 1.0.1
 */
public class ConnectableFieldProcessor implements FieldProcessor.Processor {

    private final Field[] fields;
    private final String connector;

    public ConnectableFieldProcessor(Field field, String connector) {
        this.fields = ColumnUtil.initFields(field.getType(), FieldProcessor::init);
        this.connector = connector;
    }

    @Override
    public String process(List<Object> argList, Object value) {
        StringJoiner joiner = new StringJoiner(connector, OP, CP);
        for (Field subField : fields) {
            Object subValue = CommonUtil.readField(subField, value);
            if (QuerySuffix.isValidValue(subValue, subField)) {
                String condition = execute(subField, argList, subValue);
                if (StringUtils.isNotEmpty(condition)) {
                    joiner.add(condition);
                }
            }
        }
        String clause = joiner.toString();
        return "()".equals(clause) ? null : clause;
    }
}
