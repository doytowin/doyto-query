package win.doyto.query.menu;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import win.doyto.query.mybatis.QueryMapper;

/**
 * MenuMapper
 *
 * @author f0rb
 * @date 2019-05-13
 */
@Mapper
interface MenuMapper extends QueryMapper<MenuEntity, Integer, MenuQuery> {

    @Select("select * from menu where id = #{id}")
    MenuEntity get(@Param("id")Integer id);

}
