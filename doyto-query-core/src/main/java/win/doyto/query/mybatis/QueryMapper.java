package win.doyto.query.mybatis;

import org.apache.ibatis.annotations.SelectProvider;
import win.doyto.query.core.QueryBuilder;

import java.util.List;

/**
 * QueryMapper
 *
 * @author f0rb
 * @date 2019-05-12
 */
public interface QueryMapper<E, Q> {
    @SelectProvider(type = QueryBuilder.class, method = "buildSelect")
    List<E> query(Q query);
}
