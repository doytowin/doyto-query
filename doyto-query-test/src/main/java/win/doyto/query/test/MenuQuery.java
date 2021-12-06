package win.doyto.query.test;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import win.doyto.query.annotation.NestedQueries;
import win.doyto.query.annotation.NestedQuery;

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
public class MenuQuery extends TestPageQuery {

    @NestedQueries({
            @NestedQuery(select = "menuId", from = "t_perm_and_menu pm", extra = "inner join t_perm p on p.id = pm.perm_id and p.valid = true"),
            @NestedQuery(select = "permId", from = "t_role_and_perm rp", extra = "inner join t_role r on r.id = rp.role_id and r.valid = true"),
            @NestedQuery(select = "roleId", from = "t_user_and_role"),
    })
    private Integer userId;

    @NestedQueries({
            @NestedQuery(select = "parent_id", from = "menu")
    })
    private boolean onlyParent;

    @NestedQueries({
            @NestedQuery(select = "parent_id", from = "menu")
    })
    private MenuQuery parent;

    private String nameLike;

    private Boolean valid;

    private boolean parentIdNull;
}
