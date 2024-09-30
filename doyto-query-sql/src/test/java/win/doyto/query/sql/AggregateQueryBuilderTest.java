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
import win.doyto.query.sql.q9.ProductTypeProfitMeasureHaving;
import win.doyto.query.sql.q9.ProductTypeProfitMeasureView;
import win.doyto.query.sql.q9.ProfitQuery;
import win.doyto.query.test.tpch.domain.lineitem.LineitemQuery;
import win.doyto.query.test.user.MaxIdView;
import win.doyto.query.test.user.UserLevelCountView;
import win.doyto.query.test.user.UserLevelHaving;

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
    void q9ProductTypeProfitMeasureQuery() {
        GlobalConfiguration.instance().setTableFormat("%s");

        try {
            String expected = "SELECT nation, o_year, SUM(amount) AS sum_profit" +
                    " FROM " +
                    "(SELECT n_name AS nation, YEAR(o_orderdate) AS o_year," +
                    " l_extendedprice * (1 - l_discount) - ps_supplycost * l_quantity AS amount" +
                    " FROM part, supplier, lineitem, partsupp, orders, nation" +
                    " WHERE s_nationkey = n_nationkey" +
                    " AND l_orderkey = o_orderkey" +
                    " AND l_suppkey = s_suppkey" +
                    " AND l_partkey = p_partkey" +
                    " AND ps_suppkey = l_suppkey" +
                    " AND ps_partkey = l_partkey" +
                    " AND p_name LIKE ?" +
                    ") AS profit" +
                    " GROUP BY nation, o_year" +
                    " ORDER BY nation, o_year DESC";

            ProfitQuery profitQuery = ProfitQuery.builder().pNameLike("green").build();
            ProductTypeProfitMeasureHaving having = ProductTypeProfitMeasureHaving
                    .builder().profitQuery(profitQuery).sort("nation;o_year,DESC").build();

            EntityMetadata em = EntityMetadata.build(ProductTypeProfitMeasureView.class);
            SqlAndArgs sqlAndArgs = AggregateQueryBuilder.buildSelectAndArgs(em, having);

            assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
            assertThat(sqlAndArgs.getArgs()).containsExactly("%green%");
        } finally {
            GlobalConfiguration.instance().setTableFormat("t_%s");
        }
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

        TopSuppliedHaving having = TopSuppliedHaving
                .builder().total_revenue(new PageQuery()).lineitemRevenueQuery(lineitemQuery)
                .sort("s_suppkey").build();

        EntityMetadata em = EntityMetadata.build(TopSupplierView.class);
        SqlAndArgs sqlAndArgs = AggregateQueryBuilder.buildSelectAndArgs(em, having);

        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(startShipdate, endShipdate);
    }
}
