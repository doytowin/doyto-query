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

package win.doyto.query.web.component;

import win.doyto.query.core.DoytoQuery;
import win.doyto.query.util.ColumnUtil;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;

import static win.doyto.query.core.QuerySuffix.isValidValue;
import static win.doyto.query.util.CommonUtil.readFieldGetter;

/**
 * NotEmptyQueryValidator
 *
 * @author f0rb on 2022/12/10
 * @since 1.0.1
 */
public class NotEmptyQueryValidator implements ConstraintValidator<NotEmptyQuery, DoytoQuery> {
    public boolean isValid(DoytoQuery query, ConstraintValidatorContext constraintValidatorContext) {
        if (query.needPaging()) {
            return true;
        }
        Field[] fields = ColumnUtil.queryFields(query.getClass());
        for (Field field : fields) {
            Object value = readFieldGetter(field, query);
            if (isValidValue(value, field)) {
                return true;
            }
        }
        return false;
    }
}
