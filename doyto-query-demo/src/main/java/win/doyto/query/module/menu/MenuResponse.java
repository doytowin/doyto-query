package win.doyto.query.module.menu;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.common.BeanUtil;

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

    public static MenuResponse of(MenuEntity menuEntity) {
        return BeanUtil.copyFields(menuEntity, MenuResponse.class);
    }
}
