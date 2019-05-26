package win.doyto.query.module.menu;

import org.springframework.stereotype.Service;
import win.doyto.query.core.AbstractCrudService;

/**
 * MenuService
 *
 * @author f0rb
 */
@Service
class MenuService extends AbstractCrudService<MenuEntity, Integer, MenuQuery> {

    @Override
    protected String getCacheName() {
        return "module:menu";
    }
}
