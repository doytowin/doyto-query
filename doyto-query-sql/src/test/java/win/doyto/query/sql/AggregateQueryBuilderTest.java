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

package win.doyto.query.sql;

import org.junit.jupiter.api.Test;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.PageQuery;
import win.doyto.query.sql.q15.TopSuppliedHaving;
import win.doyto.query.sql.q15.TopSupplierView;
import win.doyto.query.test.tpch.domain.lineitem.LineitemQuery;
import win.doyto.query.test.user.MaxIdView;
import win.doyto.query.test.user.UserLevelCountView;
import win.doyto.query.test.user.UserLevelHaving;
import win.doyto.query.test.user.UserLevelQuery;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;

import java.sql.Date;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AggregateQueryBuilderTest
 *
 * @author f0rb on 2024/8/12
 */
class AggregateQueryBuilderTest {

    @Test
    void supportAggregateQuery() {
        EntityMetadata em = EntityMetadata.build(MaxIdView.class);
        SqlAndArgs sqlAndArgs = AggregateQueryBuilder.buildSelectAndArgs(em, new PageQuery());
        assertThat(sqlAndArgs.getSql()).isEqualTo("SELECT max(id) AS maxId, first(create_user_id) AS firstCreateUserId FROM t_user");
    }

    @Test
    void buildSelectAndArgs() {
        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(false);

        try {
            EntityMetadata entityMetadata = EntityMetadata.build(UserLevelCountView.class);
            UserLevelHaving query = UserLevelHaving.builder().countGt(1).countLt(10).valid(true).build();
            SqlAndArgs sqlAndArgs = AggregateQueryBuilder.buildSelectAndArgs(entityMetadata, query);

            String expected = "SELECT userLevel, valid, count(*) AS count FROM t_user WHERE valid = ? " +
                    "GROUP BY userLevel, valid HAVING count(*) > ? AND count(*) < ?";

            assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
            assertThat(sqlAndArgs.getArgs()).containsExactly(true, 1, 10);
        } finally {
            GlobalConfiguration.instance().setMapCamelCaseToUnderscore(true);
        }
    }

    @Test
    void q15TopSupplierQuery() {
        String expected = "SELECT s_suppkey, s_name, s_address, s_phone, total_revenue FROM" +
                " (SELECT l_suppkey AS supplier_no," +
                " SUM(l_extendedprice * (1 - l_discount)) AS total_revenue" +
                " FROM t_lineitem" +
                " WHERE l_shipdate >= ?" +
                " AND l_shipdate < ?" +
                " GROUP BY l_suppkey) AS t_revenue, t_supplier" +
                " WHERE supplier_no = s_suppkey" +
                " ORDER BY s_suppkey";

        Date startShipdate = Date.valueOf(LocalDate.of(1995, 1, 1));
        Date endShipdate = Date.valueOf(LocalDate.of(1995, 4, 1));
        LineitemQuery lineitemQuery = LineitemQuery
                .builder()
                .l_shipdateGe(startShipdate)
                .l_shipdateLt(endShipdate)
                .build();

        TopSuppliedHaving having = TopSuppliedHaving
                .builder().revenueQuery(lineitemQuery)
                .sort("s_suppkey").build();

        EntityMetadata em = EntityMetadata.build(TopSupplierView.class);
        SqlAndArgs sqlAndArgs = AggregateQueryBuilder.buildSelectAndArgs(em, having);

        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(startShipdate, endShipdate);
    }
}
