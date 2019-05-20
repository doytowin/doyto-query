package win.doyto.query.module.menu;

import org.apache.ibatis.annotations.Mapper;
import win.doyto.query.mybatis.CrudMapper;
import win.doyto.query.mybatis.MapperTable;

/**
 * MenuMapper
 *
 * @author f0rb
 */
@Mapper
@MapperTable(MenuEntity.TABLE)
interface MenuMapper extends CrudMapper<MenuEntity, Integer, MenuQuery> {

}
