package win.doyto.query.demo.module.menu;

import lombok.Getter;
import lombok.Setter;

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

}
