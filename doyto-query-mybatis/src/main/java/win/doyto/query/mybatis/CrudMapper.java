package win.doyto.query.mybatis;

import org.apache.ibatis.annotations.*;
import win.doyto.query.core.DataAccess;
import win.doyto.query.core.QueryBuilder;

import java.util.List;

/**
 * CrudMapper
 *
 * @author f0rb
 * @date 2019-05-12
 */
public interface CrudMapper<E, I, Q> extends DataAccess<E, I, Q> {

    @SelectProvider(type = QueryBuilder.class, method = "buildSelect")
    List<E> query(Q query);

    @SelectProvider(type = QueryBuilder.class, method = "buildCount")
    long count(Q query);

    @Lang(MapperTableDriver.class)
    @Select("SELECT * FROM @{table} WHERE id = #{id}")
    E get(@Param("id") I id);

    @Lang(MapperTableDriver.class)
    @Select("DELETE FROM @{table} WHERE id = #{id}")
    void delete(I id);

    @InsertProvider(type = CrudBuilder.class, method = "create")
    @Options(useGeneratedKeys = true)
    void create(E e);

    @Override
    @UpdateProvider(type = CrudBuilder.class, method = "update")
    void update(E e);
}
