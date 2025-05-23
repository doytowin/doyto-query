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

import win.doyto.query.annotation.DomainPath;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.sql.BuildHelper;
import win.doyto.query.util.ColumnUtil;

import java.lang.reflect.Field;
import java.util.List;

import static win.doyto.query.sql.Constant.*;

/**
 * ExistsProcessor
 *
 * @author f0rb on 2023/1/4
 * @since 1.0.1
 */
public class ExistsProcessor implements FieldProcessor {

    private final String clauseFormat;
    private final String foreignAlias;

    public ExistsProcessor(Field field) {
        DomainPath domainPath = field.getAnnotation(DomainPath.class);
        String domain = GlobalConfiguration.formatTable(domainPath.value()[0]);
        String primaryId = ColumnUtil.convertColumn(domainPath.localField());
        String foreignId = ColumnUtil.convertColumn(domainPath.foreignField());
        String localAlias = domainPath.localAlias();
        foreignAlias = domainPath.foreignAlias();
        clauseFormat = (field.getName().endsWith("NotExists") ? "NOT " : EMPTY) + "EXISTS"
                + OP + SELECT + "*" + FROM + domain + defaultIfNotEmpty(foreignAlias, SPACE) + foreignAlias
                + WHERE + localAlias + defaultIfNotEmpty(localAlias, ".") + primaryId + EQUAL
                + foreignAlias + defaultIfNotEmpty(foreignAlias, ".") + foreignId + "%s" + CP;
    }

    private static String defaultIfNotEmpty(String text, String defaults) {
        return text.isEmpty() ? EMPTY : defaults;
    }

    @Override
    public String process(String alias, List<Object> argList, Object query) {
        return String.format(clauseFormat, BuildHelper.buildCondition(AND, query, argList, this.foreignAlias));
    }
}
