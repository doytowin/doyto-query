package win.doyto.query.menu;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
class MenuController {

    MenuService menuService;

    @GetMapping("page")
    public PageList<MenuEntity> page(MenuQuery menuQuery) {
        return menuService.page(menuQuery);
    }
}
