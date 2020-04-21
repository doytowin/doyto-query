package win.doyto.query.core.test;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import win.doyto.query.annotation.NestedQueries;
import win.doyto.query.annotation.NestedQuery;
import win.doyto.query.core.PageQuery;

import java.util.List;

/**
 * PermissionQuery
 *
 * @author f0rb on 2019-05-28
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionQuery extends PageQuery {

    @NestedQueries({
            @NestedQuery(select = "permId", from = "t_role_and_perm"),
            @NestedQuery(select = "roleId", from = "t_user_and_role"),
    })
    private Integer userId;

    @NestedQueries(value = {
            @NestedQuery(select = "permId", from = "t_role_and_perm"),
            @NestedQuery(select = "roleId", from = "t_user_and_role ur",
                    extra = "inner join user u on u.id = ur.userId and u.valid = ?"
            )},
            appendWhere = false)
    private Boolean validUser;

    @NestedQueries(@NestedQuery(select = "permId", from = "t_role_and_perm"))
    private List<Integer> roleIdIn;

}
