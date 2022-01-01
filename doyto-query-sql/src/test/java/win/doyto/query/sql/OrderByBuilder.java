/*
 * Copyright © 2019-2022 Forb Yuan
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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * OrderByBuilder
 *
 * @author f0rb on 2020-01-01
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderByBuilder {
    public static OrderByBuilder create() {
        return new OrderByBuilder();
    }

    private final StringBuilder buffer = new StringBuilder();

    public OrderByBuilder asc(String column) {
        buffer.append(column).append(",asc;");
        return this;
    }

    public OrderByBuilder desc(String column) {
        buffer.append(column).append(",desc;");
        return this;
    }

    public OrderByBuilder field(String column, String... args) {
        buffer.append("field(").append(column).append(',')
              .append(StringUtils.join(args, ',')).append(");");
        return this;
    }

    @Override
    public String toString() {
        return buffer.deleteCharAt(buffer.length() - 1).toString();
    }

}
