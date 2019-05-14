package win.doyto.query.menu;

import org.apache.ibatis.annotations.Mapper;
import win.doyto.query.mybatis.MapperTable;
import win.doyto.query.mybatis.QueryMapper;

/**
 * MenuMapper
 *
 * @author f0rb
 * @date 2019-05-13
 */
@Mapper
@MapperTable(MenuEntity.TABLE)
interface MenuMapper extends QueryMapper<MenuEntity, Integer, MenuQuery> {

}
