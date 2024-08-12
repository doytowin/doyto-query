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
import win.doyto.query.sql.q15.LineitemRevenueQuery;
import win.doyto.query.sql.q15.TopSupplierQuery;
import win.doyto.query.sql.q15.TopSupplierView;
import win.doyto.query.test.tpch.domain.lineitem.LineitemQuery;
import win.doyto.query.test.user.UserLevelAggrQuery;
import win.doyto.query.test.user.UserLevelCountView;
import win.doyto.query.test.user.UserLevelQuery;

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
    void buildSelectAndArgs() {
        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(false);

        UserLevelQuery userLevelQuery = UserLevelQuery.builder().valid(true).build();
        UserLevelAggrQuery aggrQuery = UserLevelAggrQuery.builder().entityQuery(userLevelQuery)
                                                         .countGt(1).countLt(10).build();
        SqlAndArgs sqlAndArgs = AggregateQueryBuilder.buildSelectAndArgs(UserLevelCountView.class, aggrQuery);

        String expected = "SELECT userLevel, valid, count(*) AS count FROM t_user WHERE valid = ? " +
                "GROUP BY userLevel, valid HAVING count(*) > ? AND count(*) < ?";

        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(true, 1, 10);

        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(true);
    }

    @Test
    void q15TopSupplierQuery() {
        String expected = "WITH t_revenue AS" +
                " (SELECT l_suppkey AS supplier_no," +
                " SUM(l_extendedprice * (1 - l_discount)) AS total_revenue" +
                " FROM t_lineitem" +
                " WHERE l_shipdate >= ?" +
                " AND l_shipdate < ?" +
                " GROUP BY l_suppkey)" +
                " SELECT s_suppkey, s_name, s_address, s_phone, total_revenue" +
                " FROM t_supplier, revenue" +
                " WHERE supplier_no = s_suppkey" +
                " AND total_revenue = (SELECT MAX(total_revenue) FROM t_revenue)" +
                " ORDER BY s_suppkey";

        Date startShipdate = Date.valueOf(LocalDate.of(1995, 1, 1));
        Date endShipdate = Date.valueOf(LocalDate.of(1995, 4, 1));
        LineitemQuery lineitemQuery = LineitemQuery
                .builder()
                .l_shipdateGe(startShipdate)
                .l_shipdateLt(endShipdate)
                .build();
        TopSupplierQuery query = TopSupplierQuery
                .builder()
                .entityQuery(LineitemRevenueQuery.builder().total_revenue(new PageQuery()).build())
                .lineitemRevenueQuery(LineitemRevenueQuery.builder().entityQuery(lineitemQuery).build())
                .sort("s_suppkey")
                .build();

        SqlAndArgs sqlAndArgs = AggregateQueryBuilder.buildSelectAndArgs(TopSupplierView.class, query);

        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(startShipdate, endShipdate);
    }
}