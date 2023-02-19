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
import win.doyto.query.annotation.QueryField;

import java.lang.reflect.Field;
import java.util.List;

import static win.doyto.query.sql.Constant.PLACE_HOLDER;

/**
 * QueryFieldProcessor
 *
 * @author f0rb on 2023/2/19
 * @since 1.0.1
 */
class QueryFieldProcessor implements FieldProcessor {

    private final String andSQL;
    private final int holderCount;

    QueryFieldProcessor(Field field) {
        andSQL = field.getAnnotation(QueryField.class).and();
        holderCount = StringUtils.countMatches(andSQL, PLACE_HOLDER);
    }

    @Override
    public String process(List<Object> argList, Object value) {
        for (int i = 0; i < holderCount; i++) {
            argList.add(value);
        }
        return andSQL;
    }
}
