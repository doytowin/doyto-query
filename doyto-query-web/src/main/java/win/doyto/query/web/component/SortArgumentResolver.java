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

package win.doyto.query.web.component;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ServletModelAttributeMethodProcessor;
import win.doyto.query.core.DoytoQuery;

import java.util.Iterator;
import java.util.StringJoiner;

/**
 * SortArgumentResolver
 *
 * @author f0rb on 2023/11/30
 * @since 1.0.3
 */
public class SortArgumentResolver extends ServletModelAttributeMethodProcessor {

    public static final String SORT_PREFIX = "sort.";

    public SortArgumentResolver() {
        super(true);
    }

    @Override
    protected void bindRequestParameters(WebDataBinder binder, NativeWebRequest request) {
        super.bindRequestParameters(binder, request);
        Object target = binder.getTarget();
        if (target instanceof DoytoQuery) {
            DoytoQuery query = (DoytoQuery) target;
            Iterator<String> it = request.getParameterNames();
            StringJoiner sj = new StringJoiner(";");
            while (it.hasNext()) {
                String key = it.next();
                int indexOfDot = key.indexOf(SORT_PREFIX);
                if (indexOfDot == -1) {
                    continue;
                }
                String column = key.substring(indexOfDot + SORT_PREFIX.length());
                String value = request.getParameter(key);
                if (StringUtils.isBlank(value)) {
                    sj.add(column + ",asc");
                } else if (value.contains(",")) {
                    sj.add("field(" + column + "," + value + ")");
                } else {
                    sj.add(column + "," + value);
                }
            }
            if (sj.length() > 0) {
                query.setSort(sj.toString());
            }
        }
    }

}
