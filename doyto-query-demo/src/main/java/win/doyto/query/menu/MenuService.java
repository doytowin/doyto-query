package win.doyto.query.menu;

import org.springframework.stereotype.Service;
import win.doyto.query.mybatis.AbstractMyBatisService;

/**
 * MenuService
 *
 * @author f0rb
 * @date 2019-05-13
 */
@Service
class MenuService extends AbstractMyBatisService<MenuEntity, MenuQuery> {

    public MenuService(MenuMapper menuMapper) {
        super(menuMapper);
    }

}
