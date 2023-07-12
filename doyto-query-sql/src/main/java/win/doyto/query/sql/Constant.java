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

/**
 * Constant
 *
 * @author f0rb on 2019-06-03
 */
@SuppressWarnings({"java:S1214", "java:S115"})
public interface Constant {
    String SEPARATOR = ", ";
    String PLACE_HOLDER = "?";
    String SPACE = " ";
    String EQUAL = " = ";
    String EQUAL_HOLDER = EQUAL + PLACE_HOLDER;
    String SELECT = "SELECT ";
    String COUNT = "count(*)";
    String FROM = " FROM ";
    String WHERE = " WHERE ";
    String HAVING = " HAVING ";
    String GROUP_BY = " GROUP BY ";
    String ORDER_BY = " ORDER BY ";
    String EMPTY = "";
    String OR = " OR ";
    String DELETE_FROM = "DELETE" + FROM;
    String AND = " AND ";
    String IN = " IN ";
    String AS = " AS ";
    String LF = "\n";
    String ID = "id";
    String UNION_ALL = "\nUNION ALL\n";
    String INTERSECT = "\nINTERSECT\n";
    String OP = "(";
    String CP = ")";
    String INSERT_INTO = "INSERT INTO ";
    String VALUES = " VALUES ";
    String HAVING_PREFIX = "_having_";
    String WHERE_ = "\nWHERE ";
    String TABLE_ALIAS = "t";
    String LIKE = "LIKE";
    String NOT_LIKE = "NOT LIKE";
}
