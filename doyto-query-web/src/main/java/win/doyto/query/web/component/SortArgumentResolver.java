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

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ServletModelAttributeMethodProcessor;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.web.config.SortFieldsProperties;

import java.util.*;

/**
 * SortArgumentResolver
 *
 * @author f0rb on 2023/11/30
 * @since 1.0.3
 */
public class SortArgumentResolver extends ServletModelAttributeMethodProcessor {

    private final String sortPrefix;
    private final Map<Class<?>, Set<String>> queryColumnsMap;

    public SortArgumentResolver(SortFieldsProperties sortFieldsProperties) {
        super(true);
        this.sortPrefix = sortFieldsProperties.getSortPrefix();
        this.queryColumnsMap = sortFieldsProperties.getSortFieldsMap();
    }

    @SuppressWarnings("java:S135")
    @Override
    protected void bindRequestParameters(WebDataBinder binder, NativeWebRequest request) {
        super.bindRequestParameters(binder, request);
        if (!(binder.getTarget() instanceof DoytoQuery query)) {
            return;
        }

        Collection<String> columns = queryColumnsMap.getOrDefault(query.getClass(), Set.of());
        Iterator<String> it = request.getParameterNames();
        StringJoiner sj = new StringJoiner(";");
        int sortPrefixLen = sortPrefix.length();
        while (it.hasNext()) {
            String key = it.next();
            int dotIdx = key.indexOf(sortPrefix);
            if (dotIdx == -1 || !columns.contains(key.substring(dotIdx + sortPrefixLen))) {
                continue;
            }
            String column = key.substring(dotIdx + sortPrefixLen);
            String value = request.getParameter(key);
            if (StringUtils.isBlank(value)) {
                sj.add(column + ",asc");
            } else if (value.contains(",")) {
                sj.add("field(" + column + "," + value + ")");
            } else {
                sj.add(column + "," + value);
            }
        }
        if (!columns.isEmpty() || sj.length() > 0) {
            query.setSort(sj.toString());
        }
        query.setSort(StringUtils.trimToNull(query.getSort()));
    }

}
