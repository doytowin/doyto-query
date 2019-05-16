package win.doyto.query.menu;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    public void save(MenuRequest menuRequest) {
        menuService.save(menuRequest.toEntity());
    }
}
