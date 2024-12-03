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
 * <pre>
 * GlobalConfiguration.registerJoinTable("role", "user", "a_user_and_role");
 * GlobalConfiguration.registerJoinTable("perm", "role", "a_role_and_perm");
 * </pre>
 * <ul>
 *   <li>["role", "perm"] -&gt; ["role_id","perm_id"]/["a_role_and_perm"]</li>
 *   <li>["perm", "role"] -&gt; ["perm_id","role_id"]/["a_role_and_perm"]</li>
 *   <li>["perm", "role", "user"] -&gt; [ "perm_id","role_id","user_id"]/["a_role_and_perm","a_user_and_role"]</li>
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
    private Relation baseRelation;
    private List<Relation> relations;

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
        registerReverseJoinTable(originDomainPath);

        List<Relation> relations = new ArrayList<>();
        for (int i = 0; i < domainPath.length - 1; i++) {
            relations.add(Relation.build(domainPath[i], domainPath[i + 1]));
        }
        domainPath = cleanupDomainPath(domainPath);
        String targetTable = prepareTargetTable(domainPath);
        Relation relation = new Relation(foreignFieldColumn, targetTable, localFieldColumn);
        return new DomainPathDetail(domainPath, relation, relations);
    }

    private static String[] cleanupDomainPath(String[] domainPath) {
        return Arrays.stream(domainPath)
                     .map(entity -> {
                         if (entity.contains("<-")) {
                             return entity.substring(0, entity.indexOf("<-"));
                         } else if (entity.contains("->")) {
                             return entity.substring(0, entity.indexOf("->"));
                         }
                         return entity;
                     })
                     .toArray(String[]::new);
    }

    private static String prepareTargetTable(String[] domainPath) {
        return GlobalConfiguration.formatTable(domainPath[domainPath.length - 1]);
    }

    private static String[] prepareDomainPath(String[] originDomainPath) {
        return Arrays.stream(originDomainPath)
                     .filter(path -> !REVERSE_SIGN.equals(path))
                     .toArray(String[]::new);
    }

    private static void registerReverseJoinTable(String[] domainPath) {
        GlobalConfiguration config = GlobalConfiguration.instance();
        int i = 0;
        while (i < domainPath.length - 1) {
            if (REVERSE_SIGN.equals(domainPath[i + 1])) {
                GlobalConfiguration.registerJoinTable(domainPath[i], domainPath[i + 2],
                        config.formatJoinTable(domainPath[i + 2], domainPath[i]));
                i++;
            }
            i++;
        }
    }
}
