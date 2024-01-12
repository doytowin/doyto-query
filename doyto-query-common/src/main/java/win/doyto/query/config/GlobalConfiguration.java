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

package win.doyto.query.config;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.core.Dialect;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.util.ColumnUtil;

import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * GlobalConfiguration
 *
 * @author f0rb
 */
@Getter
@Setter
public class GlobalConfiguration {

    private boolean mapCamelCaseToUnderscore = true;
    private boolean ignoreCacheException = true;
    private String joinIdFormat = "%s_id";
    private String tableFormat;
    private Pattern tablePtn;
    private Pattern wildcardPtn = Pattern.compile("[%|_]");
    private String joinTableFormat = "a_%s_and_%s";
    private Dialect dialect = new SimpleDialect();
    private Function<Integer, Integer> startPageNumberAdjuster;

    private GlobalConfiguration() {
        this.setTableFormat("t_%s");

        // !!! The starting value of the page number is set to ONE by default since 0.3.0 !!!
        this.setStartPageNumberFromOne(true);
    }

    public static int adjustStartPageNumber(Integer page) {
        return instance().getStartPageNumberAdjuster().apply(page);
    }

    public static GlobalConfiguration instance() {
        return Singleton.instance;
    }

    public static Dialect dialect() {
        return instance().dialect;
    }

    public static int calcOffset(DoytoQuery query) {
        return GlobalConfiguration.adjustStartPageNumber(query.getPageNumber()) * query.getPageSize();
    }

    public static String formatTable(String domain) {
        if (Singleton.instance.tablePtn.matcher(domain).matches()) {
            return domain;
        }
        String table = ColumnUtil.convertTableName(domain);
        return String.format(Singleton.instance.tableFormat, table);
    }

    public boolean isOracle() {
        return dialect().getClass().getSimpleName().contains("Oracle");
    }

    public void setTableFormat(String tableFormat) {
        this.tableFormat = tableFormat;
        String regex = tableFormat.replace("%s", "[a-z_\\${}]+");
        this.tablePtn = Pattern.compile(regex);
    }

    public void setStartPageNumberFromOne(boolean startPageNumberFromOne) {
        this.startPageNumberAdjuster = startPageNumberFromOne ? page -> Math.max(page - 1, 0) : page -> page;
    }

    public String formatJoinId(String domain) {
        return String.format(joinIdFormat, domain);
    }

    public String formatJoinTable(String domain1, String domain2) {
        return String.format(joinTableFormat, domain1, domain2);
    }

    private static class Singleton {
        private static final GlobalConfiguration instance = new GlobalConfiguration();
    }
}
