package win.doyto.query.demo.module.menu;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.core.PageQuery;
import win.doyto.query.core.QueryTable;

/**
 * MenuQuery
 *
 * @author f0rb
 */
@Getter
@Setter
@QueryTable(table = MenuEntity.TABLE)
public class MenuQuery extends PageQuery {

    private Integer id;

    private String platform;

    private String menuNameLike;

}
