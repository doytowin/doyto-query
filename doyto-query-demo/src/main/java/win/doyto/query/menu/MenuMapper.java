package win.doyto.query.menu;

import org.apache.ibatis.annotations.Mapper;
import win.doyto.query.mybatis.CrudMapper;
import win.doyto.query.mybatis.MapperTable;

/**
 * MenuMapper
 *
 * @author f0rb
 * @date 2019-05-13
 */
@Mapper
@MapperTable(MenuEntity.TABLE)
interface MenuMapper extends CrudMapper<MenuEntity, Integer, MenuQuery> {

}
