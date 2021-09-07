package win.doyto.query.demo.module.menu;

import org.springframework.stereotype.Service;
import win.doyto.query.service.AbstractDynamicService;

/**
 * MenuService
 *
 * @author f0rb on 2020-04-18
 */
@Service
public class MenuService extends AbstractDynamicService<MenuEntity, Integer, MenuQuery> {

    @Override
    protected String getCacheName() {
        return "module:menu";
    }

}
