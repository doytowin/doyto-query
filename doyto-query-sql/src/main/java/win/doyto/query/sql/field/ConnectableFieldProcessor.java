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

import win.doyto.query.sql.BuildHelper;
import win.doyto.query.util.ColumnUtil;

import java.lang.reflect.Field;
import java.util.List;

import static win.doyto.query.sql.Constant.*;

/**
 * ConnectableFieldProcessor
 *
 * @author f0rb on 2023/2/19
 * @since 1.0.1
 */
public class ConnectableFieldProcessor implements FieldProcessor {

    private final Field[] fields;
    private final String connector;

    public ConnectableFieldProcessor(Class<?> fieldType, String connector) {
        this.fields = ColumnUtil.initFields(fieldType, FieldMapper::init);
        this.connector = connector;
    }

    @Override
    public String process(String alias, List<Object> argList, Object value) {
        String clause = BuildHelper.buildCondition(fields, value, argList, alias, EMPTY, connector);
        return clause.isEmpty() ? null : OP + clause + CP;
    }
}
