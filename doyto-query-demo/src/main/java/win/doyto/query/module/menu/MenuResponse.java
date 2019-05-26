package win.doyto.query.module.menu;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.entity.EntityResponse;

/**
 * MenuResponse
 *
 * @author f0rb
 */
@Getter
@Setter
public class MenuResponse implements EntityResponse<MenuEntity, MenuResponse> {

    private Integer id;

    private Integer parentId;

    private String menuName;

    private String memo;

    private Boolean valid;

    private Long createUserId;

    private Long updateUserId;

    @Override
    public MenuResponse from(MenuEntity menuEntity) {

        MenuResponse from = EntityResponse.super.from(menuEntity);
        from.setId(menuEntity.getId());
        from.setCreateUserId(menuEntity.getCreateUserId());
        from.setUpdateUserId(menuEntity.getUpdateUserId());
        return from;

    }
}
