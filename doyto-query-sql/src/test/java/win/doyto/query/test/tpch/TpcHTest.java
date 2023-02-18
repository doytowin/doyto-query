/*
 * Copyright © 2019-2023 Forb Yuan
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

package win.doyto.query.test.tpch;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.sql.RelationalQueryBuilder;
import win.doyto.query.sql.SqlAndArgs;
import win.doyto.query.test.tpch.q1.PricingSummaryQuery;
import win.doyto.query.test.tpch.q1.PricingSummaryView;
import win.doyto.query.test.tpch.q2.MinimumCostSupplierQuery;
import win.doyto.query.test.tpch.q2.MinimumCostSupplierView;
import win.doyto.query.test.tpch.q2.SupplyCostQuery;
import win.doyto.query.test.tpch.q3.ShippingPriorityQuery;
import win.doyto.query.test.tpch.q3.ShippingPriorityView;

import java.sql.Date;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TpcHTest
 *
 * @author f0rb on 2023/2/16
 * @since 1.0.1
 */
class TpcHTest {

    @BeforeAll
    static void beforeAll() {
        GlobalConfiguration.instance().setTableFormat("%s");
    }

    @AfterAll
    static void afterAll() {
        GlobalConfiguration.instance().setTableFormat("t_%s");
    }

    @Test
    void queryForPricingSummaryReport() {
        String expected = "SELECT " +
                "l_returnflag, " +
                "l_linestatus, " +
                "sum(l_quantity) AS sum_qty, " +
                "sum(l_extendedprice) AS sum_base_price, " +
                "sum(l_extendedprice*(1-l_discount)) AS sum_disc_price, " +
                "sum(l_extendedprice*(1-l_discount)*(1+l_tax)) AS sum_charge, " +
                "avg(l_quantity) AS avg_qty, " +
                "avg(l_extendedprice) AS avg_price, " +
                "avg(l_discount) AS avg_disc, " +
                "count(*) AS count_order" +
                " FROM lineitem" +
                " WHERE l_shipdate <= date '1998-12-01' - interval '?' day (3)" +
                " GROUP BY l_returnflag, l_linestatus" +
                " ORDER BY l_returnflag, l_linestatus";

        PricingSummaryQuery query = PricingSummaryQuery
                .builder()
                .shipdateDelta(90)
                .sort("l_returnflag;l_linestatus")
                .build();

        SqlAndArgs sqlAndArgs = RelationalQueryBuilder.buildSelectAndArgs(query, PricingSummaryView.class);
        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
    }

    @Test
    void queryForMinimumCostSupplier() {
        String expected = "SELECT " +
                "s_acctbal, " +
                "s_name, " +
                "n_name, " +
                "p_partkey, " +
                "p_mfgr, " +
                "s_address, " +
                "s_phone, " +
                "s_comment" +
                " FROM part, supplier, partsupp, nation, region" +
                " WHERE n_nationkey = s_nationkey" +
                " AND p_partkey = ps_partkey" +
                " AND s_suppkey = ps_suppkey" +
                " AND r_regionkey = n_regionkey" +
                " AND p_size = ?" +
                " AND p_type LIKE ?" +
                " AND r_name = ?" +
                " AND ps_supplycost = (" +
                "SELECT MIN(ps_supplycost)" +
                " FROM partsupp, supplier, nation, region" +
                " WHERE p_partkey = ps_partkey" +
                " AND s_suppkey = ps_suppkey" +
                " AND n_nationkey = s_nationkey" +
                " AND r_regionkey = n_regionkey" +
                " AND r_name = ?" +
                ") " +
                "ORDER BY s_acctbal DESC, n_name, s_name, p_partkey";

        MinimumCostSupplierQuery query = MinimumCostSupplierQuery
                .builder()
                .p_size(15)
                .p_typeEnd("BRASS")
                .r_name("EUROPE")
                .ps_supplycost(SupplyCostQuery.builder().r_name("EUROPE").build())
                .sort("s_acctbal,DESC;n_name;s_name;p_partkey")
                .build();

        SqlAndArgs sqlAndArgs = RelationalQueryBuilder.buildSelectAndArgs(query, MinimumCostSupplierView.class);
        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
    }

    @Test
    void queryForShippingPriority() {
        String expected = "SELECT " +
                "l_orderkey, " +
                "SUM(l_extendedprice * (1 - l_discount)) AS revenue, " +
                "o_orderdate, " +
                "o_shippriority" +
                " FROM customer, orders, lineitem" +
                " WHERE c_custkey = o_custkey" +
                " AND o_orderkey = l_orderkey" +
                " AND c_mktsegment = ?" +
                " AND o_orderdate < ?" +
                " AND l_shipdate > ?" +
                " GROUP BY l_orderkey, o_orderdate, o_shippriority" +
                " ORDER BY revenue DESC, o_orderdate";

        Date date = Date.valueOf(LocalDate.of(1995, 3, 15));
        ShippingPriorityQuery query = ShippingPriorityQuery
                .builder()
                .c_mktsegment("BUILDING")
                .o_orderdateLt(date)
                .l_shipdateGt(date)
                .sort("revenue,DESC;o_orderdate")
                .build();

        SqlAndArgs sqlAndArgs = RelationalQueryBuilder.buildSelectAndArgs(query, ShippingPriorityView.class);
        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
    }
}
