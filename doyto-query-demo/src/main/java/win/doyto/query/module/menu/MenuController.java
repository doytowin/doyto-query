package win.doyto.query.module.menu;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import win.doyto.query.core.CrudService;
import win.doyto.query.entity.AbstractRestController;

/**
 * MenuController
 *
 * @author f0rb
 */
@RestController
@RequestMapping("menu")
@SuppressWarnings("squid:S4529")
class MenuController extends AbstractRestController<MenuEntity, Integer, MenuQuery, MenuRequest, MenuResponse> {

    public MenuController(CrudService<MenuEntity, Integer, MenuQuery> crudService) {
        super(crudService);
    }

}
