package win.doyto.query.menu;

import lombok.Builder;
import win.doyto.query.core.NestedQueries;
import win.doyto.query.core.NestedQuery;
import win.doyto.query.core.QueryTable;

/**
 * MenuQuery
 *
 * @author f0rb on 2019-05-28
 */
@Builder
@QueryTable(table = "menu")
public class MenuQuery {

    @NestedQueries({
        @NestedQuery(left = "menuId", table = "t_perm_and_menu", right = "permId"),
        @NestedQuery(left = "permId", table = "t_role_and_perm", right = "roleId"),
        @NestedQuery(left = "roleId", table = "t_user_and_role", right = "userId"),
    })
    private Integer userId;

}
