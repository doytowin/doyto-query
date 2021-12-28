package win.doyto.query.sql;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.SerializationUtils;
import win.doyto.query.core.DoytoQuery;

import static win.doyto.query.sql.BuildHelper.*;
import static win.doyto.query.sql.Constant.*;

/**
 * JoinQueryBuilder
 *
 * @author f0rb on 2019-06-09
 */
@AllArgsConstructor
public class JoinQueryBuilder {

    private Class<?> entityClass;

    public SqlAndArgs buildJoinSelectAndArgs(DoytoQuery q) {
        return buildSelectAndArgs(q, entityClass);
    }

    public SqlAndArgs buildJoinCountAndArgs(DoytoQuery q) {
        return buildCountAndArgs(q, entityClass);
    }

    public static SqlAndArgs buildSelectAndArgs(DoytoQuery q, Class<?> entityClass) {
        return SqlAndArgs.buildSqlWithArgs(argList -> {
            DoytoQuery query = SerializationUtils.clone(q);
            EntityMetadata entityMetadata = EntityMetadata.build(entityClass);
            String sql = SELECT + entityMetadata.getColumnsForSelect() +
                    FROM + entityMetadata.getTableName() +
                    entityMetadata.resolveJoinSql(query, argList) +
                    buildWhere(query, argList) +
                    entityMetadata.getGroupBySql() +
                    buildOrderBy(query);
            return buildPaging(sql, query);
        });
    }

    public static SqlAndArgs buildCountAndArgs(DoytoQuery q, Class<?> entityClass) {
        return SqlAndArgs.buildSqlWithArgs((argList -> {
            DoytoQuery query = SerializationUtils.clone(q);
            EntityMetadata entityMetadata = EntityMetadata.build(entityClass);
            return SELECT + COUNT +
                    FROM + entityMetadata.getTableName() +
                    entityMetadata.resolveJoinSql(query, argList) +
                    buildWhere(query, argList) +
                    entityMetadata.getGroupBySql();
        }));
    }

}
