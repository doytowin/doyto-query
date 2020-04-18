package win.doyto.query.demo.module.menu;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import win.doyto.query.demo.exception.ServiceAsserts;
import win.doyto.query.service.PageList;
import win.doyto.query.util.BeanUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * MenuController
 *
 * @author f0rb
 */
@RestController
@RequestMapping("{platform}/menu")
@AllArgsConstructor
class MenuController {

    private MenuService menuService;

    public MenuEntity buildEntity(MenuRequest menuRequest) {
        return BeanUtil.convertTo(menuRequest, MenuEntity.class);
    }

    public MenuResponse buildResponse(MenuEntity menuEntity) {
        return BeanUtil.convertTo(menuEntity, MenuResponse.class);
    }

    @GetMapping
    public Object pageOrQuery(MenuQuery q) {
        return q.needPaging() ? page(q) : menuService.query(q, this::buildResponse);
    }

    public PageList<MenuResponse> page(MenuQuery q) {
        return menuService.page(q, this::buildResponse);
    }

    @GetMapping("{id}")
    public MenuResponse get(MenuRequest menuRequest) {
        MenuEntity menuEntity = menuService.get(menuRequest.toIdWrapper());
        ServiceAsserts.notNull(menuEntity, "菜单不存在");
        return buildResponse(menuEntity);
    }

    @DeleteMapping("{id}")
    public void delete(MenuRequest menuRequest) {
        MenuEntity menuEntity = menuService.delete(menuRequest.toIdWrapper());
        ServiceAsserts.notNull(menuEntity, "菜单不存在");
    }

    @PostMapping
    public void create(@RequestBody MenuRequest request, @PathVariable String platform) {
        request.setPlatform(platform);
        menuService.create(buildEntity(request));
    }

    @PostMapping("batch")
    public void create(@RequestBody List<MenuRequest> requests, @PathVariable String platform) {
        ArrayList<MenuEntity> menuEntities = new ArrayList<>(requests.size());
        for (MenuRequest request : requests) {
            request.setPlatform(platform);
            menuEntities.add(buildEntity(request));
        }
        menuService.batchInsert(menuEntities);
    }

    @PutMapping("{id}")
    public void update(@RequestBody MenuRequest request, @PathVariable String platform) {
        request.setPlatform(platform);
        menuService.update(buildEntity(request));
    }

}
