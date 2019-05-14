package win.doyto.query.menu;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

/**
 * MenuRequest
 *
 * @author f0rb
 * @date 2019-05-14
 */
@Getter
@Setter
public class MenuRequest {

    private Integer parentId;

    private String menuName;

    private String memo;

    private Boolean valid;

    public MenuEntity toEntity() {
        MenuEntity menuEntity = new MenuEntity();
        BeanUtils.copyProperties(this, menuEntity);
        return menuEntity;
    }
}
