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

package win.doyto.query.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import win.doyto.query.core.Dialect;

import java.util.function.Function;

/**
 * GlobalConfiguration
 *
 * @author f0rb
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GlobalConfiguration {

    private boolean mapCamelCaseToUnderscore;
    private boolean ignoreCacheException = true;
    private Dialect dialect = (sql, limit, offset) ->  sql + " LIMIT " + limit + " OFFSET " + offset;
    private Function<Integer, Integer> startPageNumberAdjuster;

    public static int adjustStartPageNumber(Integer page) {
        return instance().getStartPageNumberAdjuster().apply(page);
    }

    public static GlobalConfiguration instance() {
        return Singleton.instance;
    }

    private static class Singleton {
        private static final GlobalConfiguration instance = new GlobalConfiguration();

        static {
            // !!! Default to set page number starting from ONE since 0.3.0 !!!
            instance.setStartPageNumberFromOne(true);
        }
    }

    public static Dialect dialect() {
        return instance().dialect;
    }

    public void setStartPageNumberFromOne(boolean startPageNumberFromOne) {
        instance().setStartPageNumberAdjuster(page -> startPageNumberFromOne ? Math.max(page - 1, 0) : page);
    }

}
