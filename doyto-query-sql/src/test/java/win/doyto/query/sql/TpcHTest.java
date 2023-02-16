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

package win.doyto.query.sql;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.test.tpch.q1.PricingSummaryQuery;
import win.doyto.query.test.tpch.q1.PricingSummaryView;

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
}
