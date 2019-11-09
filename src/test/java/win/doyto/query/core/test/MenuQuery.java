package win.doyto.query.core.test;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
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
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MenuQuery extends PageQuery {

    @NestedQueries(value = {
        @NestedQuery(left = "menuId", table = "t_perm_and_menu pm", extra = "inner join t_perm p on p.id = pm.perm_id and p.valid = true"),
        @NestedQuery(left = "permId", table = "t_role_and_perm rp", extra = "inner join t_role r on r.id = rp.role_id and r.valid = true"),
        @NestedQuery(left = "roleId", table = "t_user_and_role"),
    })
    private Integer userId;

    @SubQuery(left = "parent_id", table = "menu")
    private boolean onlyParent;

    @SubQuery(left = "parent_id", table = "menu")
    private MenuQuery parent;

    private String nameLike;

    private Boolean valid;

    private boolean parentIdNull;
}
