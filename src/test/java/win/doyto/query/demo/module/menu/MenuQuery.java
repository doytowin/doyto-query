package win.doyto.query.demo.module.menu;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.core.PageQuery;

/**
 * MenuQuery
 *
 * @author f0rb
 */
@Getter
@Setter
public class MenuQuery extends PageQuery {

    private Integer id;

    private String platform;

    private String menuNameLike;

}
