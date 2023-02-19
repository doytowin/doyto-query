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
import win.doyto.query.test.tpch.domain.lineitem.LineitemQuery;
import win.doyto.query.test.tpch.domain.supplier.SupplierQuery;
import win.doyto.query.test.tpch.q1.PricingSummaryQuery;
import win.doyto.query.test.tpch.q1.PricingSummaryView;
import win.doyto.query.test.tpch.q10.ReturnedItemReportingQuery;
import win.doyto.query.test.tpch.q10.ReturnedItemReportingView;
import win.doyto.query.test.tpch.q11.ImportantStockIdentificationQuery;
import win.doyto.query.test.tpch.q11.ImportantStockIdentificationView;
import win.doyto.query.test.tpch.q11.ValueHaving;
import win.doyto.query.test.tpch.q11.ValueQuery;
import win.doyto.query.test.tpch.q12.ShippingModesAndOrderPriorityQuery;
import win.doyto.query.test.tpch.q12.ShippingModesAndOrderPriorityView;
import win.doyto.query.test.tpch.q14.PromotionEffectQuery;
import win.doyto.query.test.tpch.q14.PromotionEffectView;
import win.doyto.query.test.tpch.q16.PartsSupplierRelationshipQuery;
import win.doyto.query.test.tpch.q16.PartsSupplierRelationshipView;
import win.doyto.query.test.tpch.q17.SmallQuantityOrderRevenueQuery;
import win.doyto.query.test.tpch.q17.SmallQuantityOrderRevenueView;
import win.doyto.query.test.tpch.q18.LargeVolumeCustomerQuery;
import win.doyto.query.test.tpch.q18.LargeVolumeCustomerView;
import win.doyto.query.test.tpch.q18.LineitemQuantityHaving;
import win.doyto.query.test.tpch.q18.LineitemQuantityQuery;
import win.doyto.query.test.tpch.q19.DiscountedRevenueQuery;
import win.doyto.query.test.tpch.q19.DiscountedRevenueView;
import win.doyto.query.test.tpch.q19.LineitemFilter;
import win.doyto.query.test.tpch.q19.LineitemOr;
import win.doyto.query.test.tpch.q2.MinimumCostSupplierQuery;
import win.doyto.query.test.tpch.q2.MinimumCostSupplierView;
import win.doyto.query.test.tpch.q2.SupplyCostQuery;
import win.doyto.query.test.tpch.q3.ShippingPriorityQuery;
import win.doyto.query.test.tpch.q3.ShippingPriorityView;
import win.doyto.query.test.tpch.q5.LocalSupplierVolumeQuery;
import win.doyto.query.test.tpch.q5.LocalSupplierVolumeView;
import win.doyto.query.test.tpch.q6.ForecastingRevenueChangeQuery;
import win.doyto.query.test.tpch.q6.ForecastingRevenueChangeView;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;

import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.YEARS;
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
        String expected = "SELECT s_acctbal, s_name, n_name, p_partkey, p_mfgr, s_address, s_phone, s_comment" +
                " FROM part, supplier, partsupp, nation, region" +
                " WHERE s_nationkey = n_nationkey" +
                " AND ps_partkey = p_partkey" +
                " AND ps_suppkey = s_suppkey" +
                " AND n_regionkey = r_regionkey" +
                " AND p_size = ?" +
                " AND p_type LIKE ?" +
                " AND r_name = ?" +
                " AND ps_supplycost = (" +
                "SELECT MIN(ps_supplycost)" +
                " FROM partsupp, supplier, nation, region" +
                " WHERE ps_partkey = p_partkey" +
                " AND ps_suppkey = s_suppkey" +
                " AND s_nationkey = n_nationkey" +
                " AND n_regionkey = r_regionkey" +
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
                " WHERE o_custkey = c_custkey" +
                " AND l_orderkey = o_orderkey" +
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

    @Test
    void queryForLocalSupplierVolume() {
        String expected = "SELECT n_name, SUM(l_extendedprice * (1 - l_discount)) AS revenue" +
                " FROM customer, orders, lineitem, supplier, nation, region" +
                " WHERE c_nationkey = n_nationkey" +
                " AND o_custkey = c_custkey" +
                " AND l_orderkey = o_orderkey" +
                " AND l_suppkey = s_suppkey" +
                " AND s_nationkey = n_nationkey" +
                " AND n_regionkey = r_regionkey" +
                " AND r_name = ?" +
                " AND o_orderdate >= ?" +
                " AND o_orderdate < ?" +
                " GROUP BY n_name"+
                " ORDER BY revenue DESC";

        LocalDate date = LocalDate.of(1994, 1, 1);
        Date orderDateGe = Date.valueOf(date);
        Date orderDateLt = Date.valueOf(date.plus(1, YEARS));
        LocalSupplierVolumeQuery query = LocalSupplierVolumeQuery
                .builder()
                .r_name("ASIA")
                .o_orderdateGe(orderDateGe)
                .o_orderdateLt(orderDateLt)
                .sort("revenue,DESC")
                .build();

        SqlAndArgs sqlAndArgs = RelationalQueryBuilder.buildSelectAndArgs(query, LocalSupplierVolumeView.class);
        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
    }

    @Test
    void queryForForecastingRevenueChange() {
        String expected = "SELECT SUM(l_extendedprice * l_discount) AS revenue" +
                " FROM lineitem" +
                " WHERE l_shipdate >= ?" +
                " AND l_shipdate < ?" +
                " AND l_discount >= ?" +
                " AND l_discount <= ?" +
                " AND l_quantity < ?";
        LocalDate date = LocalDate.of(1994, 1, 1);

        ForecastingRevenueChangeQuery query = new ForecastingRevenueChangeQuery();
        query.setBaseShipdate(date);
        query.setBaseDiscount(BigDecimal.valueOf(0.06));
        query.setL_quantityLt(24);

        SqlAndArgs sqlAndArgs = RelationalQueryBuilder.buildSelectAndArgs(query, ForecastingRevenueChangeView.class);

        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(
                Date.valueOf(date), Date.valueOf(date.plus(1, YEARS)),
                BigDecimal.valueOf(0.05), BigDecimal.valueOf(0.07), 24);
    }

    @Test
    void queryForReturnedItemReporting() {
        String expected = "SELECT" +
                " c_custkey," +
                " c_name," +
                " SUM(l_extendedprice * (1 - l_discount)) AS revenue," +
                " c_acctbal," +
                " n_name," +
                " c_address," +
                " c_phone," +
                " c_comment" +
                " FROM customer, orders, lineitem, nation" +
                " WHERE c_nationkey = n_nationkey" +
                " AND o_custkey = c_custkey" +
                " AND l_orderkey = o_orderkey" +
                " AND o_orderdate >= ?" +
                " AND o_orderdate < ?" +
                " AND l_returnflag = ?" +
                " GROUP BY" +
                " c_custkey," +
                " c_name," +
                " c_acctbal," +
                " n_name," +
                " c_address," +
                " c_phone," +
                " c_comment" +
                " ORDER BY revenue DESC";

        LocalDate date = LocalDate.of(1994, 1, 1);

        ReturnedItemReportingQuery query = ReturnedItemReportingQuery
                .builder()
                .o_orderdateGe(Date.valueOf(date))
                .o_orderdateLt(Date.valueOf(date.plus(3, MONTHS)))
                .l_returnflag("R")
                .sort("revenue,DESC")
                .build();

        SqlAndArgs sqlAndArgs = RelationalQueryBuilder.buildSelectAndArgs(query, ReturnedItemReportingView.class);

        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(
                Date.valueOf(date), Date.valueOf(date.plus(3, MONTHS)), "R");
    }

    @Test
    void queryForImportantStockIdentification() {
        String expected = "SELECT" +
                " ps_partkey," +
                " SUM(ps_supplycost * ps_availqty) AS value" +
                " FROM partsupp, supplier, nation" +
                " WHERE ps_suppkey = s_suppkey" +
                " AND s_nationkey = n_nationkey" +
                " AND n_name = ?" +
                " GROUP BY ps_partkey" +
                " HAVING value > (SELECT SUM(ps_supplycost * ps_availqty) * 0.0001000000e-2" +
                " FROM partsupp, supplier, nation" +
                " WHERE ps_suppkey = s_suppkey" +
                " AND s_nationkey = n_nationkey" +
                " AND n_name = ?" +
                ")" +
                " ORDER BY value DESC";

        ValueHaving having = ValueHaving
                .builder()
                .valueGt(ValueQuery.builder().n_name("GERMANY").build())
                .build();
        ImportantStockIdentificationQuery query = ImportantStockIdentificationQuery
                .builder()
                .n_name("GERMANY")
                .having(having)
                .sort("value,DESC")
                .build();

        SqlAndArgs sqlAndArgs = RelationalQueryBuilder.buildSelectAndArgs(query, ImportantStockIdentificationView.class);

        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly("GERMANY", "GERMANY");
    }

    @Test
    void queryForShippingModesAndOrderPriority() {
        String expected = "SELECT l_shipmode," +
                " SUM(CASE WHEN o_orderpriority = '1-URGENT'OR o_orderpriority = '2-HIGH'THEN 1 ELSE 0 END) AS high_line_count," +
                " SUM(CASE WHEN o_orderpriority <> '1-URGENT'AND o_orderpriority <> '2-HIGH'THEN 1 ELSE 0 END) AS low_line_count" +
                " FROM orders, lineitem" +
                " WHERE l_orderkey = o_orderkey" +
                " AND l_shipmode IN (?, ?)" +
                " AND l_commitdate < l_receiptdate" +
                " AND l_shipdate < l_commitdate" +
                " AND l_receiptdate >= ?" +
                " AND l_receiptdate < ?" +
                " GROUP BY l_shipmode" +
                " ORDER BY l_shipmode";

        LocalDate date = LocalDate.of(1994, 1, 1);
        ShippingModesAndOrderPriorityQuery query = ShippingModesAndOrderPriorityQuery
                .builder()
                .l_shipmodeIn(Arrays.asList("MAIL", "SHIP"))
                .l_receiptdateGe(Date.valueOf(date))
                .l_receiptdateLt(Date.valueOf(date.plus(1, YEARS)))
                .sort("l_shipmode")
                .build();

        SqlAndArgs sqlAndArgs = RelationalQueryBuilder.buildSelectAndArgs(query, ShippingModesAndOrderPriorityView.class);

        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(
                "MAIL", "SHIP",
                Date.valueOf(date), Date.valueOf(date.plus(1, YEARS))
        );
    }

    @Test
    void queryForPromotionEffect() {
        String expected = "SELECT" +
                " 100.00 * SUM(CASE WHEN p_type LIKE 'PROMO%'THEN l_extendedprice * (1 - l_discount)ELSE 0 END) / SUM(l_extendedprice * (1 - l_discount)) AS promo_revenue" +
                " FROM lineitem, part" +
                " WHERE l_partkey = p_partkey" +
                " AND l_shipdate >= ?" +
                " AND l_shipdate < ?";

        LocalDate date = LocalDate.of(1995, 12, 1);
        PromotionEffectQuery query = PromotionEffectQuery
                .builder()
                .l_shipdateGe(Date.valueOf(date))
                .l_shipdateLt(Date.valueOf(date.plus(1, MONTHS)))
                .build();

        SqlAndArgs sqlAndArgs = RelationalQueryBuilder.buildSelectAndArgs(query, PromotionEffectView.class);

        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
    }

    @Test
    void queryForPartsSupplierRelationship() {
        String expected = "SELECT p_brand, p_type, p_size, COUNT(DISTINCT ps_suppkey) AS supplier_cnt" +
                " FROM partsupp, part" +
                " WHERE ps_partkey = p_partkey" +
                " AND p_brand != ?" +
                " AND p_type NOT LIKE ?" +
                " AND p_size IN (?, ?, ?, ?, ?, ?, ?, ?)" +
                " AND ps_suppkey NOT IN (SELECT s_suppkey" +
                " FROM supplier" +
                " WHERE s_comment LIKE ?)" +
                " GROUP BY p_brand, p_type, p_size" +
                " ORDER BY supplier_cnt DESC, p_brand, p_type, p_size";

        PartsSupplierRelationshipQuery query = PartsSupplierRelationshipQuery
                .builder()
                .p_brandNot("Brand#45")
                .p_typeNotStart("MEDIUM POLISHED")
                .p_sizeIn(Arrays.asList(9, 14, 23, 45, 19, 3, 36, 9))
                .ps_suppkeyNotIn(SupplierQuery.builder().s_commentLike("%Customer%Complaints%").build())
                .sort("supplier_cnt,DESC;p_brand;p_type;p_size")
                .build();

        SqlAndArgs sqlAndArgs = RelationalQueryBuilder.buildSelectAndArgs(query, PartsSupplierRelationshipView.class);

        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(
                "Brand#45", "MEDIUM POLISHED%", 9, 14, 23, 45, 19, 3, 36, 9, "%Customer%Complaints%"
        );
    }

    @Test
    void queryForSmallQuantityOrderRevenue() {
        String expected = "SELECT SUM(l_extendedprice) / 7.0 AS avg_yearly" +
                " FROM lineitem, part" +
                " WHERE l_partkey = p_partkey" +
                " AND p_brand = ?" +
                " AND p_container = ?" +
                " AND l_quantity < (SELECT 2e-1 * AVG(l_quantity)" +
                " FROM lineitem" +
                " WHERE l_partkey = p_partkey)";

        SmallQuantityOrderRevenueQuery query = SmallQuantityOrderRevenueQuery
                .builder()
                .p_brand("Brand#23")
                .p_container("MED BOX")
                .l_quantityLt(LineitemQuery.builder().l_partkeyEqP_partkey(true).build())
                .build();

        SqlAndArgs sqlAndArgs = RelationalQueryBuilder.buildSelectAndArgs(query, SmallQuantityOrderRevenueView.class);

        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly("Brand#23", "MED BOX");
    }

    @Test
    void queryForLargeVolumeCustomer() {
        String expected = "SELECT" +
                " c_name," +
                " c_custkey," +
                " o_orderkey," +
                " o_orderdate," +
                " o_totalprice," +
                " sum(l_quantity) AS sumL_quantity" +
                " FROM customer, orders, lineitem" +
                " WHERE o_custkey = c_custkey" +
                " AND l_orderkey = o_orderkey" +
                " AND o_orderkey IN (SELECT l_orderkey" +
                " FROM lineitem" +
                " GROUP BY l_orderkey" +
                " HAVING sum(l_quantity) > ?" +
                ")" +
                " GROUP BY c_name, c_custkey, o_orderkey, o_orderdate, o_totalprice" +
                " ORDER BY o_totalprice desc, o_orderdate";

        LineitemQuantityHaving lqHaving = LineitemQuantityHaving.builder().sumL_quantityGt(300).build();
        LineitemQuantityQuery lqQuery = LineitemQuantityQuery.builder().having(lqHaving).build();
        LargeVolumeCustomerQuery query = LargeVolumeCustomerQuery
                .builder()
                .o_orderkeyIn(lqQuery)
                .sort("o_totalprice,desc;o_orderdate")
                .build();

        SqlAndArgs sqlAndArgs = RelationalQueryBuilder.buildSelectAndArgs(query, LargeVolumeCustomerView.class);

        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(300);
    }

    @Test
    void queryForDiscountedRevenue() {
        String expected = "SELECT" +
                " SUM(l_extendedprice * (1 - l_discount)) AS revenue" +
                " FROM lineitem, part" +
                " WHERE l_partkey = p_partkey" +
                " AND ((p_brand = ?" +
                " AND p_container IN (?, ?, ?, ?)" +
                " AND l_quantity >= ?" +
                " AND l_quantity <= ?" +
                " AND p_size >= ?" +
                " AND p_size <= ?" +
                " AND l_shipmode IN (?, ?)" +
                ") OR (p_brand = ?" +
                " AND p_container IN (?, ?, ?, ?)" +
                " AND l_quantity >= ?" +
                " AND l_quantity <= ?" +
                " AND p_size >= ?" +
                " AND p_size <= ?" +
                " AND l_shipmode IN (?, ?)" +
                ") OR (p_brand = ?" +
                " AND p_container IN (?, ?, ?, ?)" +
                " AND l_quantity >= ?" +
                " AND l_quantity <= ?" +
                " AND p_size >= ?" +
                " AND p_size <= ?" +
                " AND l_shipmode IN (?, ?)" +
                "))" +
                " AND l_shipinstruct = ?";

        LineitemFilter lineitemFilter1 = LineitemFilter
                .builder()
                .p_brand("Brand#12")
                .p_containerIn(Arrays.asList("SM CASE", "SM BOX", "SM PACK", "SM PKG"))
                .l_quantityGe(0)
                .l_quantityLe(10)
                .p_sizeGe(1)
                .p_sizeLe(5)
                .l_shipmodeIn(Arrays.asList("AIR", "AIR REG"))
                .build();
        LineitemFilter lineitemFilter2 = LineitemFilter
                .builder()
                .p_brand("Brand#23")
                .p_containerIn(Arrays.asList("MED BAG", "MED BOX", "MED PKG", "MED PACK"))
                .l_quantityGe(10)
                .l_quantityLe(20)
                .p_sizeGe(1)
                .p_sizeLe(10)
                .l_shipmodeIn(Arrays.asList("AIR", "AIR REG"))
                .build();
        LineitemFilter lineitemFilter3 = LineitemFilter
                .builder()
                .p_brand("Brand#34")
                .p_containerIn(Arrays.asList("LG CASE", "LG BOX", "LG PACK", "LG PKG"))
                .l_quantityGe(20)
                .l_quantityLe(30)
                .p_sizeGe(1)
                .p_sizeLe(15)
                .l_shipmodeIn(Arrays.asList("AIR", "AIR REG"))
                .build();
        LineitemOr lineitemOr = LineitemOr
                .builder()
                .lineitemFilter1(lineitemFilter1)
                .lineitemFilter2(lineitemFilter2)
                .lineitemFilter3(lineitemFilter3)
                .lineitemFilter4(new LineitemFilter())
                .build();
        DiscountedRevenueQuery query = DiscountedRevenueQuery
                .builder()
                .lineitemOr(lineitemOr)
                .l_shipinstruct("DELIVER IN PERSON")
                .build();

        SqlAndArgs sqlAndArgs = RelationalQueryBuilder.buildSelectAndArgs(query, DiscountedRevenueView.class);

        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
    }

}
