package win.doyto.query.core.test;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import win.doyto.query.annotation.NestedQueries;
import win.doyto.query.annotation.NestedQuery;
import win.doyto.query.annotation.SubQuery;
import win.doyto.query.core.PageQuery;

/**
 * MenuQuery
 *
 * @author f0rb on 2019-05-28
 */
@Getter
@Setter
@Builder
public class MenuQuery extends PageQuery {

    @NestedQueries(value = {
        @NestedQuery(left = "menuId", table = "t_perm_and_menu pm", extra = "inner join t_perm p on p.id = pm.perm_id and p.valid = true"),
        @NestedQuery(left = "permId", table = "t_role_and_perm rp", extra = "inner join t_role r on r.id = rp.role_id and r.valid = true"),
        @NestedQuery(left = "roleId", table = "t_user_and_role"),
    })
    private Integer userId;

    @SubQuery(left = "parent_id", table = "menu")
    private boolean onlyParent;

    private boolean parentIdNull;
}
