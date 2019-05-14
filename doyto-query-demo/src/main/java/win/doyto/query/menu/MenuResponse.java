package win.doyto.query.menu;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

/**
 * MenuResponse
 *
 * @author f0rb
 * @date 2019-05-14
 */
@Getter
@Setter
public class MenuResponse {

    private Integer id;

    private Integer parentId;

    private String menuName;

    private String memo;

    private Boolean valid;

    public static MenuResponse of(MenuEntity menuEntity) {
        MenuResponse menuResponse = new MenuResponse();
        BeanUtils.copyProperties(menuEntity, menuResponse);
        return menuResponse;
    }
}
