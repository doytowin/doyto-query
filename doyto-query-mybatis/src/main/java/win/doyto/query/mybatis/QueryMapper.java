package win.doyto.query.mybatis;

import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import win.doyto.query.core.DataAccess;
import win.doyto.query.core.QueryBuilder;

import java.util.List;

/**
 * QueryMapper
 *
 * @author f0rb
 * @date 2019-05-12
 */
public interface QueryMapper<E, I, Q> extends DataAccess<E, I, Q> {

    @SelectProvider(type = QueryBuilder.class, method = "buildSelect")
    List<E> query(Q query);

    @SelectProvider(type = QueryBuilder.class, method = "buildCount")
    long count(Q query);

    @Lang(MapperTableDriver.class)
    @Select("SELECT * FROM @{table} WHERE id = #{id}")
    E get(@Param("id") I id);

}
