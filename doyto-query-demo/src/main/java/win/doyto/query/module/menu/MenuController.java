package win.doyto.query.module.menu;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import win.doyto.query.core.PageList;

/**
 * MenuController
 *
 * @author f0rb
 */
@RestController
@RequestMapping("{platform}/menu")
@AllArgsConstructor
@SuppressWarnings("squid:S4529")
class MenuController {

    MenuService menuService;

    private MenuResponse getEntityView() {
        return new MenuResponse();
    }

    @GetMapping
    public Object query(MenuQuery q) {
        if (q.needPaging()) {
            return page(q);
        }
        return menuService.query(q, getEntityView()::from);
    }

    public PageList<MenuResponse> page(MenuQuery q) {
        return menuService.page(q, getEntityView()::from);
    }

    @GetMapping("{id}")
    public MenuResponse get(MenuQuery menuRequest) {
        MenuEntity e = menuService.get(menuRequest);
        if (e == null) {
            throw new IllegalArgumentException("Record not found");
        }
        return getEntityView().from(e);
    }

    @DeleteMapping("{id}")
    public void delete(MenuRequest menuRequest) {
        MenuEntity e = menuService.delete(menuRequest.toEntity());
        if (e == null) {
            throw new IllegalArgumentException("Record not found");
        }
    }

    @PostMapping
    public void create(@RequestBody MenuRequest request, @PathVariable String platform) {
        request.setPlatform(platform);
        menuService.create(request.toEntity());
    }

    @PutMapping("{id}")
    public void update(@RequestBody MenuRequest request, @PathVariable String platform) {
        request.setPlatform(platform);
        menuService.update(request.toEntity());
    }

}
