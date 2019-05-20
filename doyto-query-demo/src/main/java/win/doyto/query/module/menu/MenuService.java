package win.doyto.query.module.menu;

import org.springframework.stereotype.Service;
import win.doyto.query.mybatis.AbstractMyBatisService;

/**
 * MenuService
 *
 * @author f0rb
 */
@Service
class MenuService extends AbstractMyBatisService<MenuEntity, Integer, MenuQuery> {

    public MenuService(MenuMapper menuMapper) {
        super(menuMapper);
    }

    @Override
    protected String getCacheName() {
        return "module:menu";
    }
}
