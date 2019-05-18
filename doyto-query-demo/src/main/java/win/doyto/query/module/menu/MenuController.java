package win.doyto.query.module.menu;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import win.doyto.query.core.PageList;

/**
 * MenuController
 *
 * @author f0rb
 * @date 2019-05-13
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
        return MenuResponse.of(menuService.get(id));
    }

    @PostMapping("save")
    public void save(@RequestBody MenuRequest menuRequest) {
        menuService.save(menuRequest.toEntity());
    }
}
