package win.doyto.query.mybatis;

import org.apache.ibatis.annotations.*;
import win.doyto.query.core.DataAccess;
import win.doyto.query.core.QueryBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * CrudMapper
 *
 * @author f0rb
 */
public interface CrudMapper<E, I extends Serializable, Q> extends DataAccess<E, I, Q> {

    @SelectProvider(type = QueryBuilder.class, method = "buildSelect")
    List<E> query(Q query);

    @SelectProvider(type = QueryBuilder.class, method = "buildCount")
    long count(Q query);

    @Lang(MapperTableDriver.class)
    @Select("SELECT * FROM @{table} WHERE id = #{id}")
    E get(@Param("id") I id);

    @Override
    @Lang(MapperTableDriver.class)
    @Select("SELECT * FROM @{table} WHERE id = #{id}")
    @Options(useCache = false)
    E fetch(@Param("id") I id);

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
