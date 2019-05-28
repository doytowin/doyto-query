package win.doyto.query.module.menu;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

/**
 * MenuResponse
 *
 * @author f0rb
 */
@Getter
@Setter
public class MenuResponse {

    private Integer id;

    private Integer parentId;

    private String menuName;

    private String memo;

    private Boolean valid;

    private Long createUserId;

    private Long updateUserId;

    public static MenuResponse build(MenuEntity menuEntity) {
        MenuResponse menuResponse = new MenuResponse();
        BeanUtils.copyProperties(menuEntity, menuResponse);
        menuResponse.setId(menuEntity.getId());
        menuResponse.setCreateUserId(menuEntity.getCreateUserId());
        menuResponse.setUpdateUserId(menuEntity.getUpdateUserId());
        return menuResponse;

    }
}
