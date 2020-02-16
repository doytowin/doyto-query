package win.doyto.query.demo.module.menu;

import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import win.doyto.query.demo.common.BeanUtil;
import win.doyto.query.demo.exception.ServiceAsserts;
import win.doyto.query.service.AbstractDynamicService;
import win.doyto.query.service.PageList;

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
class MenuController extends AbstractDynamicService<MenuEntity, Integer, MenuQuery> {

    public MenuEntity buildEntity(MenuRequest menuRequest) {
        return BeanUtil.copyFields(menuRequest, MenuEntity.class);
    }

    public MenuResponse buildResponse(MenuEntity menuEntity) {
        MenuResponse menuResponse = new MenuResponse();
        BeanUtils.copyProperties(menuEntity, menuResponse);
        menuResponse.setId(menuEntity.getId());
        menuResponse.setCreateUserId(menuEntity.getCreateUserId());
        menuResponse.setUpdateUserId(menuEntity.getUpdateUserId());
        return menuResponse;
    }

    @Override
    protected String getCacheName() {
        return "module:menu";
    }

    @GetMapping
    public Object pageOrQuery(MenuQuery q) {
        return q.needPaging() ? page(q) : query(q, this::buildResponse);
    }

    public PageList<MenuResponse> page(MenuQuery q) {
        return page(q, this::buildResponse);
    }

    @GetMapping("{id}")
    public MenuResponse get(MenuRequest menuRequest) {
        MenuEntity menuEntity = get(menuRequest.toIdWrapper());
        ServiceAsserts.notNull(menuEntity, "菜单不存在");
        return buildResponse(menuEntity);
    }

    @DeleteMapping("{id}")
    public void delete(MenuRequest menuRequest) {
        MenuEntity menuEntity = delete(menuRequest.toIdWrapper());
        ServiceAsserts.notNull(menuEntity, "菜单不存在");
    }

    @PostMapping
    public void create(@RequestBody MenuRequest request, @PathVariable String platform) {
        request.setPlatform(platform);
        create(buildEntity(request));
    }

    @PostMapping("import")
    public void create(@RequestBody List<MenuRequest> requests, @PathVariable String platform) {
        ArrayList<MenuEntity> menuEntities = new ArrayList<>(requests.size());
        for (MenuRequest request : requests) {
            request.setPlatform(platform);
            menuEntities.add(buildEntity(request));
        }
        batchInsert(menuEntities);
    }

    @PutMapping("{id}")
    public void update(@RequestBody MenuRequest request, @PathVariable String platform) {
        request.setPlatform(platform);
        update(buildEntity(request));
    }

}
