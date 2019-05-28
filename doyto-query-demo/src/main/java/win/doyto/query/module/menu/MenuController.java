package win.doyto.query.module.menu;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
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
        MenuEntity e = get(menuQuery);
        if (e == null) {
            throw new IllegalArgumentException("Record not found");
        }
        return MenuResponse.build(e);
    }

    @DeleteMapping("{id}")
    public void delete(MenuRequest menuRequest) {
        MenuEntity e = delete(menuRequest.toEntity());
        if (e == null) {
            throw new IllegalArgumentException("Record not found");
        }
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
