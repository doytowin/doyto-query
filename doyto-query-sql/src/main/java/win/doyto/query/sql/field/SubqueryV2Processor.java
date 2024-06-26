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

import win.doyto.query.annotation.SubqueryV2;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.sql.RelationalQueryBuilder;

import java.lang.reflect.Field;
import java.util.List;

import static win.doyto.query.sql.Constant.CP;
import static win.doyto.query.sql.Constant.OP;

/**
 * SubqueryV2Processor
 *
 * @author f0rb on 2024/6/26
 * @since 1.0.4
 */
public class SubqueryV2Processor implements FieldProcessor {

    private final Class<?> viewClass;
    private final String cond;

    public SubqueryV2Processor(Field field) {
        viewClass = field.getAnnotation(SubqueryV2.class).value();
        cond = SubqueryProcessor.resolveCond(field.getName());
    }

    @Override
    public String process(String alias, List<Object> argList, Object value) {
        String sql = RelationalQueryBuilder.buildSelect((DoytoQuery) value, viewClass, argList);
        return cond + OP + sql + CP;
    }
}
