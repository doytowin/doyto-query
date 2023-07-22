/*
 * Copyright © 2019-2022 Forb Yuan
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
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.datasource.DataSourceUtils;
import win.doyto.query.sql.SqlAndArgs;

import javax.sql.DataSource;
import java.sql.*;
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

    private final DataSource dataSource;

    private static void setParameters(SqlAndArgs sqlAndArgs, PreparedStatement ps) throws SQLException {
        Object[] args = sqlAndArgs.getArgs();
        for (int i = 0; i < args.length; i++) {
            ps.setObject(i + 1, args[i]);
        }
    }

    @Override
    public <V> List<V> query(SqlAndArgs sqlAndArgs, RowMapper<V> rowMapper) {
        return withTransaction(dataSource, connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlAndArgs.getSql())) {
                setParameters(sqlAndArgs, preparedStatement);
                ResultSetExtractor<List<V>> rse = new RowMapperResultSetExtractor<>(rowMapper);
                ResultSet rs = preparedStatement.executeQuery();
                return rse.extractData(rs);
            }
        });
    }

    @Override
    public long count(SqlAndArgs sqlAndArgs) {
        return withTransaction(dataSource, connection -> {
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
        return withTransaction(dataSource, connection -> {
            try (PreparedStatement ps = connection.prepareStatement(sqlAndArgs.getSql(), Statement.RETURN_GENERATED_KEYS)) {
                LinkedList<I> idList = new LinkedList<>();
                setParameters(sqlAndArgs, ps);
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                while (rs.next()) {
                    idList.add(rs.getObject(idColumn, idClass));
                }
                return idList;
            }
        });
    }

    @Override
    public int update(SqlAndArgs sqlAndArgs) {
        return withTransaction(dataSource, connection -> {
            try (PreparedStatement ps = connection.prepareStatement(sqlAndArgs.getSql())) {
                setParameters(sqlAndArgs, ps);
                return ps.executeUpdate();
            }
        });
    }

    @Override
    public <R> R query(SqlAndArgs sqlAndArgs, ResultSetExtractor<R> resultSetExtractor) {
        return withTransaction(dataSource, connection -> {
            try (PreparedStatement ps = connection.prepareStatement(sqlAndArgs.getSql())) {
                setParameters(sqlAndArgs, ps);
                ResultSet rs = ps.executeQuery();
                return resultSetExtractor.extractData(rs);
            }
        });
    }

    @SuppressWarnings("java:S112")
    private static <T> T withTransaction(DataSource dataSource, SQLExecutor<T> sql) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            return sql.execute(connection);
        } catch (SQLException e) {
            DataSourceUtils.releaseConnection(connection, dataSource);
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private interface SQLExecutor<T> {
        T execute(Connection connection) throws SQLException;
    }
}
