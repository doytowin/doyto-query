package win.doyto.query.core.module.menu;

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
        @NestedQuery(left = "menuId", table = "t_perm_and_menu pm", right = "permId", extra = "inner join t_perm p on p.id = pm.perm_id and p.valid = true"),
        @NestedQuery(left = "permId", table = "t_role_and_perm rp", right = "roleId", extra = "inner join t_role r on r.id = rp.role_id and r.valid = true"),
        @NestedQuery(left = "roleId", table = "t_user_and_role", right = "userId"),
    })
    private Integer userId;

    @NestedQuery(left = "parent_id", table = "menu", right = "true")
    private boolean onlyParent;
}
