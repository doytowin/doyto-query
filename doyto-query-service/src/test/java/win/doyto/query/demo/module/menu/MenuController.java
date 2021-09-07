package win.doyto.query.demo.module.menu;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import win.doyto.query.web.controller.AbstractDynamicController;
import win.doyto.query.web.response.JsonBody;

/**
 * MenuController
 *
 * @author f0rb
 */
@JsonBody
@RestController
@RequestMapping("{platform}/menu")
class MenuController extends AbstractDynamicController<MenuEntity, Integer, MenuQuery, MenuRequest, MenuResponse, MenuIdWrapper> {

    public MenuController(MenuService menuService) {
        super(menuService, new TypeReference<MenuIdWrapper>() {});
    }

}
