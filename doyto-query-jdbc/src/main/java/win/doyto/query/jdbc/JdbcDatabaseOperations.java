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

package win.doyto.query.jdbc;

import lombok.AllArgsConstructor;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.jdbc.rowmapper.ResultSetExtractor;
import win.doyto.query.jdbc.rowmapper.RowMapper;
import win.doyto.query.sql.SqlAndArgs;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * JdbcDatabaseOperations
 *
 * @author f0rb on 2022/12/12
 * @since 1.1.0
 */
@AllArgsConstructor
public class JdbcDatabaseOperations implements DatabaseOperations {

    private final TransactionExecutor transactionExecutor;

    private static void setParameters(SqlAndArgs sqlAndArgs, PreparedStatement ps) throws SQLException {
        Object[] args = sqlAndArgs.getArgs();
        for (int i = 0; i < args.length; i++) {
            ps.setObject(i + 1, args[i]);
        }
    }

    @Override
    public <V> List<V> query(SqlAndArgs sqlAndArgs, RowMapper<V> rowMapper) {
        return transactionExecutor.withTransaction(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlAndArgs.getSql())) {
                setParameters(sqlAndArgs, preparedStatement);
                ResultSet rs = preparedStatement.executeQuery();

                List<V> results = new ArrayList<>(rs.getFetchSize());
                int rowNum = 0;
                while (rs.next()) {
                    results.add(rowMapper.map(rs, rowNum++));
                }
                return results;
            }
        });
    }

    @Override
    public long count(SqlAndArgs sqlAndArgs) {
        return transactionExecutor.withTransaction(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlAndArgs.getSql())) {
                setParameters(sqlAndArgs, preparedStatement);
                ResultSet resultSet = preparedStatement.executeQuery();
                resultSet.next();
                return resultSet.getLong(1);
            }
        });
    }

    @Override
    public <I> List<I> insert(SqlAndArgs sqlAndArgs, Class<I> idClass, String idColumn) {
        return transactionExecutor.withTransaction(connection -> {
            boolean isOracle = GlobalConfiguration.instance().isOracle();
            try (PreparedStatement ps = isOracle ?
                    connection.prepareStatement(sqlAndArgs.getSql(), new String[]{idColumn}) :
                    connection.prepareStatement(sqlAndArgs.getSql(), Statement.RETURN_GENERATED_KEYS)
            ) {
                LinkedList<I> idList = new LinkedList<>();
                setParameters(sqlAndArgs, ps);
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                int idIndex = isOracle ? 1 : rs.findColumn(idColumn);
                while (rs.next()) {
                    idList.add(rs.getObject(idIndex, idClass));
                }
                return idList;
            }
        });
    }

    @Override
    public int update(SqlAndArgs sqlAndArgs) {
        return transactionExecutor.withTransaction(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(sqlAndArgs.getSql())) {
                setParameters(sqlAndArgs, ps);
                return ps.executeUpdate();
            }
        });
    }

    @Override
    public <R> R query(SqlAndArgs sqlAndArgs, ResultSetExtractor<R> resultSetExtractor) {
        return transactionExecutor.withTransaction(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(sqlAndArgs.getSql())) {
                setParameters(sqlAndArgs, ps);
                ResultSet rs = ps.executeQuery();
                return resultSetExtractor.extract(rs);
            }
        });
    }

}
