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

package win.doyto.query.relation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.util.ColumnUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * DomainPathDetail
 * <p>
 * Transform domain paths to join-tables
 * <p>
 * Examples:
 * <ul>
 *   <li>["role", "perm"] -&gt; ["role_id","perm_id"]/["a_role_and_perm"]</li>
 *   <li>["perm", "~", "role"] -&gt; ["perm_id","role_id"]/["a_role_and_perm"]</li>
 *   <li>["perm", "~", "role", "~", "user"] -&gt; [ "perm_id","role_id","user_id"]/["a_role_and_perm","a_user_and_role"]</li>
 * </ul>
 *
 * @author f0rb on 2022/11/19
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public class DomainPathDetail {

    private static final String REVERSE_SIGN = "~";

    private final String[] domainPath;
    private final String localFieldColumn;
    private final String foreignFieldColumn;
    private final String[] joinIds;
    private final String[] joinTables;
    private final String targetTable;

    public int getLastDomainIndex() {
        return domainPath.length - 1;
    }

    public boolean onlyOneDomain() {
        return domainPath.length == 1;
    }

    public static DomainPathDetail buildBy(DomainPath domainPathAnno) {
        return buildBy(domainPathAnno, ColumnUtil::convertColumn);
    }

    public static DomainPathDetail buildBy(DomainPath domainPathAnno, UnaryOperator<String> columnConverter) {
        return buildBy(
                domainPathAnno.value(),
                domainPathAnno.localField(),
                domainPathAnno.foreignField(),
                columnConverter
        );
    }

    public static DomainPathDetail buildBy(String[] originDomainPath, String localField, String foreignField, UnaryOperator<String> fieldConvertor) {
        String foreignFieldColumn = fieldConvertor.apply(foreignField);
        String localFieldColumn = fieldConvertor.apply(localField);
        String[] domainPath = prepareDomainPath(originDomainPath);
        String[] joinIds = prepareJoinIds(domainPath);
        if (domainPath.length == 1) {
            joinIds[0] = foreignField.equals("id") ? localFieldColumn : foreignFieldColumn;
        }
        String[] joinTables = prepareJoinTablesWithReverseSign(originDomainPath);
        String targetTable = prepareTargetTable(domainPath);
        return new DomainPathDetail(domainPath, localFieldColumn, foreignFieldColumn, joinIds, joinTables, targetTable);
    }

    private static String prepareTargetTable(String[] domainPath) {
        String tableFormat = GlobalConfiguration.instance().getTableFormat();
        return String.format(tableFormat, domainPath[domainPath.length - 1]);
    }

    private static String[] prepareDomainPath(String[] originDomainPath) {
        return Arrays.stream(originDomainPath)
                     .filter(path -> !REVERSE_SIGN.equals(path))
                     .toArray(String[]::new);
    }

    private static String[] prepareJoinIds(String[] domainPath) {
        String joinIdFormat = GlobalConfiguration.instance().getJoinIdFormat();
        return Arrays.stream(domainPath)
                     .map(domain -> String.format(joinIdFormat, domain))
                     .toArray(String[]::new);
    }

    private static String[] prepareJoinTablesWithReverseSign(String[] domainPath) {
        String joinTableFormat = GlobalConfiguration.instance().getJoinTableFormat();
        List<String> joinTableList = new ArrayList<>();
        int i = 0;
        while (i < domainPath.length - 1) {
            String domain = domainPath[i];
            String nextDomain = domainPath[i + 1];
            if (REVERSE_SIGN.equals(nextDomain)) {
                nextDomain = domain;
                domain = domainPath[i + 2];
                i++;
            }
            joinTableList.add(String.format(joinTableFormat, domain, nextDomain));
            i++;
        }
        return joinTableList.toArray(new String[0]);
    }
}
