package win.doyto.query.module.menu;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.common.BeanUtil;

/**
 * MenuRequest
 *
 * @author f0rb
 */
@Getter
@Setter
public class MenuRequest {

    private Integer id;

    private Integer parentId;

    private String menuName;

    private String memo;

    private Boolean valid = true;

    public MenuEntity toEntity() {
        return BeanUtil.copyFields(this, MenuEntity.class);
    }
}
