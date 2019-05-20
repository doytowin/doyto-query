package win.doyto.query.module.menu;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import win.doyto.query.core.PageList;
import win.doyto.query.exception.ServiceException;

/**
 * MenuController
 *
 * @author f0rb
 */
@RestController
@RequestMapping("menu")
@AllArgsConstructor
@SuppressWarnings("squid:S4529")
class MenuController {

    MenuService menuService;

    @GetMapping("page")
    public PageList<MenuResponse> page(MenuQuery menuQuery) {
        return menuService.page(menuQuery, MenuResponse::of);
    }

    @GetMapping("get")
    public MenuResponse get(Integer id) {
        MenuEntity menuEntity = menuService.get(id);
        if (menuEntity == null) {
            throw new ServiceException("菜单不存在");
        }
        return MenuResponse.of(menuEntity);
    }

    @PostMapping("save")
    public void save(@RequestBody MenuRequest menuRequest) {
        menuService.save(menuRequest.toEntity());
    }
}
