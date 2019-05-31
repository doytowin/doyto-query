package win.doyto.query.demo.module.menu;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import win.doyto.query.demo.exception.ServiceAsserts;
import win.doyto.query.service.AbstractDynamicService;
import win.doyto.query.service.PageList;

/**
 * MenuController
 *
 * @author f0rb
 */
@RestController
@RequestMapping("{platform}/menu")
@AllArgsConstructor
@SuppressWarnings("squid:S4529")
class MenuController extends AbstractDynamicService<MenuEntity, Integer, MenuQuery> {

    @Override
    protected String getCacheName() {
        return "module:menu";
    }

    @GetMapping
    public Object list(MenuQuery q) {
        return q.needPaging() ? page(q) : query(q, MenuResponse::build);
    }

    public PageList<MenuResponse> page(MenuQuery q) {
        return page(q, MenuResponse::build);
    }

    @GetMapping("{id}")
    public MenuResponse getByQuery(MenuQuery menuQuery) {
        MenuEntity menuEntity = get(menuQuery);
        ServiceAsserts.notNull(menuEntity, "菜单不存在");
        return MenuResponse.build(menuEntity);
    }

    @DeleteMapping("{id}")
    public void delete(MenuRequest menuRequest) {
        MenuEntity menuEntity = delete(menuRequest.toEntity());
        ServiceAsserts.notNull(menuEntity, "菜单不存在");
    }

    @PostMapping
    public void create(@RequestBody MenuRequest request, @PathVariable String platform) {
        request.setPlatform(platform);
        create(request.toEntity());
    }

    @PutMapping("{id}")
    public void update(@RequestBody MenuRequest request, @PathVariable String platform) {
        request.setPlatform(platform);
        update(request.toEntity());
    }

}
