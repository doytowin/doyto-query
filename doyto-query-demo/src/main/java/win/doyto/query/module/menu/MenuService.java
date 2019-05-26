package win.doyto.query.module.menu;

import org.springframework.stereotype.Service;
import win.doyto.query.core.AbstractDynamicService;

/**
 * MenuService
 *
 * @author f0rb
 */
@Service
class MenuService extends AbstractDynamicService<MenuEntity, Integer, MenuQuery> {

    @Override
    protected String getCacheName() {
        return "module:menu";
    }
}
