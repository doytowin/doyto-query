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

import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.util.ColumnUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * DomainPathDetail
 * <p>
 * Transform domain paths to join-tables
 * <p>
 * Examples:
 * <ul>
 *   <li>["role", "perm"] -> ["role_id","perm_id"]/["a_role_and_perm"]</li>
 *   <li>["perm", "~", "role"] -> ["perm_id","role_id"]/["a_role_and_perm"]</li>
 *   <li>["perm", "~", "role", "~", "user"] -> [ "perm_id","role_id","user_id"]/["a_role_and_perm","a_user_and_role"]</li>
 * </ul>
 * </p>
 *
 * @author f0rb on 2022/11/19
 * @since 1.0.0
 */
@Getter
public class DomainPathDetail {

    private static final String JOIN_ID_FORMAT = GlobalConfiguration.instance().getJoinIdFormat();
    private static final String TABLE_FORMAT = GlobalConfiguration.instance().getTableFormat();
    private static final String JOIN_TABLE_FORMAT = GlobalConfiguration.instance().getJoinTableFormat();
    private static final String REVERSE_SIGN = "~";

    private final String[] domainPath;
    private final String[] joinIds;
    private final String[] joinTables;
    private final String targetTable;
    private final String foreignFieldColumn;
    private final String localFieldColumn;

    DomainPathDetail(DomainPath domainPathAnno, boolean reverse) {
        String[] originDomainPath = domainPathAnno.value();
        foreignFieldColumn = ColumnUtil.convertColumn(domainPathAnno.foreignField());
        localFieldColumn = ColumnUtil.convertColumn(domainPathAnno.localField());

        domainPath = Arrays.stream(originDomainPath)
                           .filter(path -> !REVERSE_SIGN.equals(path))
                           .toArray(String[]::new);
        joinIds = prepareDomainIds(domainPath);
        joinTables = prepareJoinTablesWithReverseSign(originDomainPath);
        if (!Arrays.asList(originDomainPath).contains(REVERSE_SIGN) && reverse) {
            ArrayUtils.reverse(joinIds);
            ArrayUtils.reverse(joinTables);
            ArrayUtils.reverse(domainPath);
        }
        targetTable = String.format(TABLE_FORMAT, domainPath[domainPath.length - 1]);
    }

    private static String[] prepareDomainIds(String[] domainPath) {
        return Arrays.stream(domainPath)
                     .map(domain -> String.format(DomainPathDetail.JOIN_ID_FORMAT, domain))
                     .toArray(String[]::new);
    }

    private static String[] prepareJoinTablesWithReverseSign(String[] domainPaths) {
        String joinTableFormat = GlobalConfiguration.instance().getJoinTableFormat();
        List<String> joinTableList = new ArrayList<>();
        int i = 0;
        while (i < domainPaths.length - 1) {
            String domain = domainPaths[i];
            String nextDomain = domainPaths[i + 1];
            if (REVERSE_SIGN.equals(nextDomain)) {
                nextDomain = domain;
                domain = domainPaths[i + 2];
                i++;
            }
            joinTableList.add(String.format(joinTableFormat, domain, nextDomain));
            i++;
        }
        return joinTableList.toArray(new String[0]);
    }

}
