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

package win.doyto.query.test.tpch;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.PageQuery;
import win.doyto.query.sql.AggregateQueryBuilder;
import win.doyto.query.sql.EntityMetadata;
import win.doyto.query.sql.SqlAndArgs;
import win.doyto.query.test.tpch.domain.lineitem.LineitemQuery;
import win.doyto.query.test.tpch.domain.part.PartQuery;
import win.doyto.query.test.tpch.domain.supplier.SupplierQuery;
import win.doyto.query.test.tpch.q1.PricingSummaryQuery;
import win.doyto.query.test.tpch.q1.PricingSummaryView;
import win.doyto.query.test.tpch.q10.ReturnedItemReportingQuery;
import win.doyto.query.test.tpch.q10.ReturnedItemReportingView;
import win.doyto.query.test.tpch.q11.ImportantStockIdentificationHaving;
import win.doyto.query.test.tpch.q11.ImportantStockIdentificationView;
import win.doyto.query.test.tpch.q11.ValueQuery;
import win.doyto.query.test.tpch.q12.ShippingModesAndOrderPriorityQuery;
import win.doyto.query.test.tpch.q12.ShippingModesAndOrderPriorityView;
import win.doyto.query.test.tpch.q13.CustomerDistributionQuery;
import win.doyto.query.test.tpch.q13.CustomerDistributionView;
import win.doyto.query.test.tpch.q13.CustomerOrdersQuery;
import win.doyto.query.test.tpch.q13.JoinOrders;
import win.doyto.query.test.tpch.q14.PromotionEffectQuery;
import win.doyto.query.test.tpch.q14.PromotionEffectView;
import win.doyto.query.test.tpch.q15.TopSuppliedHaving;
import win.doyto.query.test.tpch.q15.TopSupplierView;
import win.doyto.query.test.tpch.q16.PartsSupplierRelationshipQuery;
import win.doyto.query.test.tpch.q16.PartsSupplierRelationshipView;
import win.doyto.query.test.tpch.q17.SmallQuantityOrderRevenueQuery;
import win.doyto.query.test.tpch.q17.SmallQuantityOrderRevenueView;
import win.doyto.query.test.tpch.q18.LargeVolumeCustomerQuery;
import win.doyto.query.test.tpch.q18.LargeVolumeCustomerView;
import win.doyto.query.test.tpch.q18.LineitemQuantityHaving;
import win.doyto.query.test.tpch.q19.DiscountedRevenueQuery;
import win.doyto.query.test.tpch.q19.DiscountedRevenueView;
import win.doyto.query.test.tpch.q19.LineitemFilter;
import win.doyto.query.test.tpch.q2.MinimumCostSupplierQuery;
import win.doyto.query.test.tpch.q2.MinimumCostSupplierView;
import win.doyto.query.test.tpch.q2.SupplyCostQuery;
import win.doyto.query.test.tpch.q20.AvailableQtyQuery;
import win.doyto.query.test.tpch.q20.PotentialPartPromotionQuery;
import win.doyto.query.test.tpch.q20.PotentialPartPromotionView;
import win.doyto.query.test.tpch.q20.SuppkeyQuery;
import win.doyto.query.test.tpch.q21.LineitemExistsQuery;
import win.doyto.query.test.tpch.q21.SuppliersWhoKeptOrdersWaitingQuery;
import win.doyto.query.test.tpch.q21.SuppliersWhoKeptOrdersWaitingView;
import win.doyto.query.test.tpch.q22.CustsaleQuery;
import win.doyto.query.test.tpch.q22.GlobalSalesOpportunityQuery;
import win.doyto.query.test.tpch.q22.GlobalSalesOpportunityView;
import win.doyto.query.test.tpch.q3.ShippingPriorityQuery;
import win.doyto.query.test.tpch.q3.ShippingPriorityView;
import win.doyto.query.test.tpch.q4.LineitemReceiptQuery;
import win.doyto.query.test.tpch.q4.OrderPriorityCheckingQuery;
import win.doyto.query.test.tpch.q4.OrderPriorityCheckingView;
import win.doyto.query.test.tpch.q5.LocalSupplierVolumeQuery;
import win.doyto.query.test.tpch.q5.LocalSupplierVolumeView;
import win.doyto.query.test.tpch.q6.ForecastingRevenueChangeQuery;
import win.doyto.query.test.tpch.q6.ForecastingRevenueChangeView;
import win.doyto.query.test.tpch.q7.NameComparison;
import win.doyto.query.test.tpch.q7.ShippingQuery;
import win.doyto.query.test.tpch.q7.VolumeShippingQuery;
import win.doyto.query.test.tpch.q7.VolumeShippingView;
import win.doyto.query.test.tpch.q8.AllNationsQuery;
import win.doyto.query.test.tpch.q8.NationalMarketShareQuery;
import win.doyto.query.test.tpch.q8.NationalMarketShareView;
import win.doyto.query.test.tpch.q9.ProductTypeProfitMeasureHaving;
import win.doyto.query.test.tpch.q9.ProductTypeProfitMeasureView;
import win.doyto.query.test.tpch.q9.ProfitQuery;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TpcHTest
 *
 * @author f0rb on 2023/2/16
 * @since 1.0.1
 */
class TpcHTest {
    private SqlAndArgs buildSelectAndArgs(DoytoQuery query, Class<?> clazz) {
        EntityMetadata em = EntityMetadata.build(clazz);
        return AggregateQueryBuilder.buildSelectAndArgs(em, query);
    }

    @BeforeAll
    static void beforeAll() {
        GlobalConfiguration.instance().setTableFormat("%s");
    }

    @AfterAll
    static void afterAll() {
        GlobalConfiguration.instance().setTableFormat("t_%s");
    }

    @Test
    void q1PricingSummaryReportQuery() {
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
                " WHERE l_shipdate <= ?" +
                " GROUP BY l_returnflag, l_linestatus" +
                " ORDER BY l_returnflag, l_linestatus";

        Date date = Date.valueOf(LocalDate.of(1998, 9, 1));
        PricingSummaryQuery query = PricingSummaryQuery
                .builder()
                .lShipdateLe(date)
                .sort("l_returnflag;l_linestatus")
                .build();

        SqlAndArgs sqlAndArgs = buildSelectAndArgs(query, PricingSummaryView.class);
        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
    }

    @Test
    void q2MinimumCostSupplierQuery() {
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
                "SELECT min(ps_supplycost)" +
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
                .psSupplycost(SupplyCostQuery.builder().r_name("EUROPE").build())
                .sort("s_acctbal,DESC;n_name;s_name;p_partkey")
                .build();

        SqlAndArgs sqlAndArgs = buildSelectAndArgs(query, MinimumCostSupplierView.class);
        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
    }

    @Test
    void q3ShippingPriorityQuery() {
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

        SqlAndArgs sqlAndArgs = buildSelectAndArgs(query, ShippingPriorityView.class);
        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
    }

    @Test
    void q4OrderPriorityCheckingQuery() {
        String expected = "SELECT o_orderpriority, count(*) AS order_count" +
                " FROM orders t" +
                " WHERE o_orderdate >= ?" +
                " AND o_orderdate < ?" +
                " AND EXISTS(SELECT * FROM lineitem t1" +
                " WHERE t.o_orderkey = t1.l_orderkey" +
                " AND t1.l_commitdate < t1.l_receiptdate" +
                ")" +
                " GROUP BY o_orderpriority" +
                " ORDER BY o_orderpriority";

        LocalDate date = LocalDate.of(1993, 7, 1);
        Date orderDateGe = Date.valueOf(date);
        Date orderDateLt = Date.valueOf(date.plusMonths(3));
        OrderPriorityCheckingQuery query = OrderPriorityCheckingQuery
                .builder()
                .o_orderdateGe(orderDateGe)
                .o_orderdateLt(orderDateLt)
                .orderExists(new LineitemReceiptQuery())
                .sort("o_orderpriority")
                .build();

        SqlAndArgs sqlAndArgs = buildSelectAndArgs(query, OrderPriorityCheckingView.class);
        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
    }

    @Test
    void q5LocalSupplierVolumeQuery() {
        String expected = "SELECT n_name, SUM(l_extendedprice * (1 - l_discount)) AS revenue" +
                " FROM customer, orders, lineitem, supplier, nation, region" +
                " WHERE c_nationkey = n_nationkey" +
                " AND o_custkey = c_custkey" +
                " AND l_orderkey = o_orderkey" +
                " AND l_suppkey = s_suppkey" +
                " AND n_regionkey = r_regionkey" +
                " AND c_nationkey = s_nationkey" +
                " AND r_name = ?" +
                " AND o_orderdate >= ?" +
                " AND o_orderdate < ?" +
                " GROUP BY n_name" +
                " ORDER BY revenue DESC";

        LocalDate date = LocalDate.of(1994, 1, 1);
        Date orderDateGe = Date.valueOf(date);
        Date orderDateLt = Date.valueOf(date.plusYears(1));
        LocalSupplierVolumeQuery query = LocalSupplierVolumeQuery
                .builder()
                .rName("ASIA")
                .oOrderdateGe(orderDateGe)
                .oOrderdateLt(orderDateLt)
                .sort("revenue,DESC")
                .build();

        SqlAndArgs sqlAndArgs = buildSelectAndArgs(query, LocalSupplierVolumeView.class);
        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
    }

    @Test
    void q6ForecastingRevenueChangeQuery() {
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

        SqlAndArgs sqlAndArgs = buildSelectAndArgs(query, ForecastingRevenueChangeView.class);

        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(
                Date.valueOf(date), Date.valueOf(date.plusYears(1)),
                BigDecimal.valueOf(0.05), BigDecimal.valueOf(0.07), 24);
    }

    @Test
    void q7VolumeShippingQuery() {
        String expected = "SELECT supp_nation," +
                " cust_nation," +
                " l_year," +
                " SUM(volume) AS revenue" +
                " FROM (SELECT n1.n_name AS supp_nation," +
                " n2.n_name AS cust_nation," +
                " YEAR(l_shipdate) AS l_year," +
                " l_extendedprice * (1 - l_discount) AS volume" +
                " FROM supplier, lineitem, orders, customer, nation n1, nation n2" +
                " WHERE s_nationkey = n1.n_nationkey" +
                " AND l_orderkey = o_orderkey" +
                " AND l_suppkey = s_suppkey" +
                " AND o_custkey = c_custkey" +
                " AND c_nationkey = n2.n_nationkey" +
                " AND ((n1.n_name = ? AND n2.n_name = ?)" +
                " OR (n1.n_name = ? AND n2.n_name = ?))" +
                " AND l_shipdate >= ? AND l_shipdate <= ?" +
                ") AS shipping" +
                " GROUP BY supp_nation, cust_nation, l_year" +
                " ORDER BY supp_nation, cust_nation, l_year";

        Date startShipdate = Date.valueOf(LocalDate.of(1995, 1, 1));
        Date endShipdate = Date.valueOf(LocalDate.of(1996, 12, 31));

        ShippingQuery shippingQuery = ShippingQuery
                .builder()
                .nameOr(Arrays.asList(
                        new NameComparison("FRANCE", "GERMANY"),
                        new NameComparison("GERMANY", "FRANCE")
                ))
                .l_shipdateGe(startShipdate)
                .l_shipdateLe(endShipdate)
                .build();
        VolumeShippingQuery query = VolumeShippingQuery
                .builder()
                .shippingQuery(shippingQuery)
                .sort("supp_nation;cust_nation;l_year")
                .build();
        SqlAndArgs sqlAndArgs = buildSelectAndArgs(query, VolumeShippingView.class);

        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(
                "FRANCE", "GERMANY",
                "GERMANY", "FRANCE",
                startShipdate, endShipdate);
    }

    @Test
    void q8NationalMarketShareQuery() {
        String expected = "SELECT" +
                " o_year," +
                " SUM(CASE WHEN nation = ? THEN volume ELSE 0 END) / SUM(volume) AS mkt_share" +
                " FROM" +
                " (SELECT" +
                " YEAR(o_orderdate) AS o_year," +
                " l_extendedprice * (1 - l_discount) AS volume," +
                " n2.n_name AS nation" +
                " FROM part, lineitem, orders, customer, supplier, nation n1, nation n2, region" +
                " WHERE l_orderkey = o_orderkey" +
                " AND l_suppkey = s_suppkey" +
                " AND l_partkey = p_partkey" +
                " AND o_custkey = c_custkey" +
                " AND c_nationkey = n1.n_nationkey" +
                " AND s_nationkey = n2.n_nationkey" +
                " AND n1.n_regionkey = r_regionkey" +
                " AND r_name = ?" +
                " AND o_orderdate >= ?" +
                " AND o_orderdate <= ?" +
                " AND p_type = ?" +
                ") AS all_nations" +
                " GROUP BY o_year" +
                " ORDER BY o_year";

        Date startShipdate = Date.valueOf(LocalDate.of(1995, 1, 1));
        Date endShipdate = Date.valueOf(LocalDate.of(1996, 12, 31));

        AllNationsQuery allNationsQuery = AllNationsQuery
                .builder()
                .r_name("AMERICA")
                .o_orderdateGe(startShipdate)
                .o_orderdateLe(endShipdate)
                .p_type("ECONOMY ANODIZED STEEL")
                .build();

        NationalMarketShareQuery query = NationalMarketShareQuery
                .builder()
                .nationEq("BRAZIL")
                .allNationsQuery(allNationsQuery).sort("o_year")
                .build();
        SqlAndArgs sqlAndArgs = buildSelectAndArgs(query, NationalMarketShareView.class);

        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(
                "BRAZIL", "AMERICA", startShipdate, endShipdate, "ECONOMY ANODIZED STEEL");
    }

    @Test
    void q9ProductTypeProfitMeasureQuery() {
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

        SqlAndArgs sqlAndArgs = buildSelectAndArgs(having, ProductTypeProfitMeasureView.class);

        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly("%green%");
    }

    @Test
    void q10ReturnedItemReportingQuery() {
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
                .o_orderdateLt(Date.valueOf(date.plusMonths(3)))
                .l_returnflag("R")
                .sort("revenue,DESC")
                .build();

        SqlAndArgs sqlAndArgs = buildSelectAndArgs(query, ReturnedItemReportingView.class);

        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(
                Date.valueOf(date), Date.valueOf(date.plusMonths(3)), "R");
    }

    @Test
    void q11ImportantStockIdentificationQuery() {
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

        ImportantStockIdentificationHaving query = ImportantStockIdentificationHaving
                .builder()
                .n_name("GERMANY")
                .valueGt(ValueQuery.builder().n_name("GERMANY").build())
                .sort("value,DESC")
                .build();

        SqlAndArgs sqlAndArgs = buildSelectAndArgs(query, ImportantStockIdentificationView.class);

        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly("GERMANY", "GERMANY");
    }

    @Test
    void q12ShippingModesAndOrderPriorityQuery() {
        String expected = "SELECT l_shipmode," +
                " SUM(CASE WHEN o_orderpriority = ? OR o_orderpriority = ? THEN 1 ELSE 0 END) AS high_line_count," +
                " SUM(CASE WHEN o_orderpriority <> ? AND o_orderpriority <> ? THEN 1 ELSE 0 END) AS low_line_count" +
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
                .o_orderpriority1("1-URGENT")
                .o_orderpriority2("2-HIGH")
                .l_shipmodeIn(Arrays.asList("MAIL", "SHIP"))
                .l_receiptdateGe(Date.valueOf(date))
                .l_receiptdateLt(Date.valueOf(date.plusYears(1)))
                .sort("l_shipmode")
                .build();

        SqlAndArgs sqlAndArgs = buildSelectAndArgs(query, ShippingModesAndOrderPriorityView.class);

        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(
                "1-URGENT", "2-HIGH",
                "1-URGENT", "2-HIGH",
                "MAIL", "SHIP",
                Date.valueOf(date), Date.valueOf(date.plusYears(1))
        );
    }

    @Test
    void q13CustomerDistributionQuery() {
        String expected = "SELECT c_count, COUNT(*) AS custdist" +
                " FROM" +
                " (SELECT c_custkey," +
                " COUNT(o_orderkey) AS c_count" +
                " FROM customer c" +
                " LEFT JOIN orders o ON" +
                " o.o_custkey = c.c_custkey AND" +
                " o.o_comment NOT LIKE ?" +
                " GROUP BY c_custkey) AS customer_orders" +
                " GROUP BY c_count" +
                " ORDER BY custdist DESC, c_count DESC";

        JoinOrders joinOrders = JoinOrders.builder().oCommentNotLike("%special%packages%").build();
        CustomerOrdersQuery customerOrdersQuery = CustomerOrdersQuery.builder().joinOrders(joinOrders).build();
        CustomerDistributionQuery query = CustomerDistributionQuery
                .builder()
                .customerOrdersQuery(customerOrdersQuery)
                .sort("custdist,DESC;c_count,DESC")
                .build();
        SqlAndArgs sqlAndArgs = buildSelectAndArgs(query, CustomerDistributionView.class);

        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly("%special%packages%");
    }

    @Test
    void q14PromotionEffectQuery() {
        String expected = "SELECT" +
                " 100.00 * SUM(CASE WHEN p_type LIKE ? THEN l_extendedprice * (1 - l_discount) ELSE 0 END)" +
                " / SUM(l_extendedprice * (1 - l_discount)) AS promo_revenue" +
                " FROM lineitem, part" +
                " WHERE l_partkey = p_partkey" +
                " AND l_shipdate >= ?" +
                " AND l_shipdate < ?";

        Date startShipdate = Date.valueOf(LocalDate.of(1995, 12, 1));
        Date endShipdate = Date.valueOf(LocalDate.of(1995, 12, 31));
        PromotionEffectQuery query = PromotionEffectQuery
                .builder()
                .pTypeStart("PROMO")
                .l_shipdateGe(startShipdate)
                .l_shipdateLt(endShipdate)
                .build();

        SqlAndArgs sqlAndArgs = buildSelectAndArgs(query, PromotionEffectView.class);

        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly("PROMO%", startShipdate, endShipdate);
    }

    @Test
    void q15TopSupplierQuery() {
        String expected = "WITH revenue AS" +
                " (SELECT l_suppkey AS supplier_no," +
                " SUM(l_extendedprice * (1 - l_discount)) AS total_revenue" +
                " FROM lineitem" +
                " WHERE l_shipdate >= ?" +
                " AND l_shipdate < ?" +
                " GROUP BY l_suppkey)" +
                " SELECT s_suppkey, s_name, s_address, s_phone, total_revenue" +
                " FROM supplier, revenue" +
                " WHERE supplier_no = s_suppkey" +
                " AND total_revenue = (SELECT MAX(total_revenue) FROM revenue)" +
                " ORDER BY s_suppkey";

        Date startShipdate = Date.valueOf(LocalDate.of(1995, 1, 1));
        Date endShipdate = Date.valueOf(LocalDate.of(1995, 4, 1));
        LineitemQuery lineitemQuery = LineitemQuery
                .builder()
                .l_shipdateGe(startShipdate)
                .l_shipdateLt(endShipdate)
                .build();

        TopSuppliedHaving having = TopSuppliedHaving
                .builder().total_revenue(new PageQuery())
                .revenueQuery(lineitemQuery)
                .sort("s_suppkey").build();

        SqlAndArgs sqlAndArgs = buildSelectAndArgs(having, TopSupplierView.class);

        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(startShipdate, endShipdate);
    }

    @Test
    void q16PartsSupplierRelationshipQuery() {
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

        SqlAndArgs sqlAndArgs = buildSelectAndArgs(query, PartsSupplierRelationshipView.class);

        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(
                "Brand#45", "MEDIUM POLISHED%", 9, 14, 23, 45, 19, 3, 36, 9, "%Customer%Complaints%"
        );
    }

    @Test
    void q17SmallQuantityOrderRevenueQuery() {
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
                .l_quantityLt(LineitemQuery.builder().build())
                .build();

        SqlAndArgs sqlAndArgs = buildSelectAndArgs(query, SmallQuantityOrderRevenueView.class);

        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly("Brand#23", "MED BOX");
    }

    @Test
    void q18LargeVolumeCustomerQuery() {
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
        LargeVolumeCustomerQuery query = LargeVolumeCustomerQuery
                .builder()
                .o_orderkeyIn(lqHaving)
                .sort("o_totalprice,desc;o_orderdate")
                .build();

        SqlAndArgs sqlAndArgs = buildSelectAndArgs(query, LargeVolumeCustomerView.class);

        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(300);
    }

    @Test
    void q19DiscountedRevenueQuery() {
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
        DiscountedRevenueQuery query = DiscountedRevenueQuery
                .builder()
                .lineitemOr(Arrays.asList(lineitemFilter1, lineitemFilter2, lineitemFilter3))
                .l_shipinstruct("DELIVER IN PERSON")
                .build();

        SqlAndArgs sqlAndArgs = buildSelectAndArgs(query, DiscountedRevenueView.class);

        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
    }

    @Test
    void q20PotentialPartPromotionQuery() {
        String expected = "SELECT s_name, s_address" +
                " FROM supplier, nation" +
                " WHERE s_nationkey = n_nationkey" +
                " AND s_suppkey IN (SELECT ps_suppkey" +
                " FROM partsupp" +
                " WHERE ps_partkey IN (SELECT p_partkey" +
                " FROM part" +
                " WHERE p_name LIKE ?)" +
                " AND ps_availqty > (SELECT 0.5 * SUM(l_quantity)" +
                " FROM lineitem" +
                " WHERE l_partkey = ps_partkey" +
                " AND l_suppkey = ps_suppkey" +
                " AND l_shipdate >= ?" +
                " AND l_shipdate < ?))" +
                " AND n_name = ?" +
                " ORDER BY s_name";

        PartQuery partQuery = PartQuery.builder().p_nameStart("forest").build();

        LocalDate date = LocalDate.of(1994, 1, 1);
        AvailableQtyQuery availableQtyQuery = AvailableQtyQuery
                .builder()
                .l_shipdateGe(Date.valueOf(date))
                .l_shipdateLt(Date.valueOf(date.plusYears(1)))
                .build();

        SuppkeyQuery suppkeyQuery = SuppkeyQuery
                .builder()
                .ps_partkeyIn(partQuery)
                .ps_availqtyGt(availableQtyQuery)
                .build();
        PotentialPartPromotionQuery query = PotentialPartPromotionQuery
                .builder()
                .s_suppkeyIn(suppkeyQuery)
                .n_name("CANADA")
                .sort("s_name")
                .build();

        SqlAndArgs sqlAndArgs = buildSelectAndArgs(query, PotentialPartPromotionView.class);

        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(
                "forest%", Date.valueOf(date),
                Date.valueOf(date.plusYears(1)), "CANADA");
    }

    @Test
    void q21SuppliersWhoKeptOrdersWaitingQuery() {
        String expected = "SELECT" +
                " s_name, count(*) AS numwait" +
                " FROM supplier, lineitem l1, orders, nation" +
                " WHERE s_nationkey = n_nationkey" +
                " AND l1.l_orderkey = o_orderkey" +
                " AND l1.l_suppkey = s_suppkey" +
                " AND n_name = ?" +
                " AND o_orderstatus = ?" +
                " AND l1.l_receiptdate > l1.l_commitdate" +
                " AND EXISTS(SELECT *" +
                " FROM lineitem l2" +
                " WHERE l1.l_orderkey = l2.l_orderkey" +
                " AND l2.l_suppkey <> l1.l_suppkey)" +
                " AND NOT EXISTS(SELECT *" +
                " FROM lineitem l3" +
                " WHERE l1.l_orderkey = l3.l_orderkey" +
                " AND l3.l_suppkey <> l1.l_suppkey" +
                " AND l3.l_receiptdate > l3.l_commitdate)" +
                " GROUP BY s_name" +
                " ORDER BY numwait DESC, s_name";

        SuppliersWhoKeptOrdersWaitingQuery query = SuppliersWhoKeptOrdersWaitingQuery
                .builder()
                .o_orderstatus("F")
                .n_name("CANADA")
                .lineitemExists(new LineitemExistsQuery())
                .lineitemNotExists(LineitemExistsQuery.builder().alias$lReceiptdateGtAlias$lCommitdate(true).build())
                .sort("numwait,DESC;s_name")
                .build();

        SqlAndArgs sqlAndArgs = buildSelectAndArgs(query, SuppliersWhoKeptOrdersWaitingView.class);

        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly("CANADA", "F");
    }

    @Test
    void q22GlobalSalesOpportunityQuery() {
        String expected = "SELECT" +
                " cntrycode, count(*) AS numcust, sum(c_acctbal) AS totacctbal" +
                " FROM (SELECT" +
                " substring(c_phone from 1 for 2) AS cntrycode, c_acctbal" +
                " FROM customer" +
                " WHERE substring(c_phone from 1 for 2) IN (?, ?)" +
                " AND c_acctbal > (SELECT avg(c_acctbal)" +
                " FROM customer" +
                " WHERE c_acctbal > ?" +
                " AND substring(c_phone from 1 for 2) IN (?, ?))" +
                " AND NOT EXISTS(SELECT * FROM orders" +
                " WHERE c_custkey = o_custkey)" +
                ") AS custsale" +
                " GROUP BY cntrycode" +
                " ORDER BY cntrycode";

        CustsaleQuery anotherQuery = CustsaleQuery
                .builder()
                .c_acctbalGt(0)
                .cntrycodeIn(Arrays.asList("28", "30"))
                .build();
        CustsaleQuery custsaleQuery = CustsaleQuery
                .builder()
                .cntrycodeIn(Arrays.asList("28", "30"))
                .c_acctbalGt2(anotherQuery)
                .ordersNotExists(new PageQuery())
                .build();
        GlobalSalesOpportunityQuery query = GlobalSalesOpportunityQuery
                .builder()
                .custsaleQuery(custsaleQuery)
                .sort("cntrycode")
                .build();

        SqlAndArgs sqlAndArgs = buildSelectAndArgs(query, GlobalSalesOpportunityView.class);

        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly("28", "30", 0, "28", "30");
    }

}
