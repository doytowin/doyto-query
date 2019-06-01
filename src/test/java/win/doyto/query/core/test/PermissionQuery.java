package win.doyto.query.core.test;

import lombok.Builder;
import win.doyto.query.core.NestedQueries;
import win.doyto.query.core.NestedQuery;
import win.doyto.query.core.QueryTable;

/**
 * PermissionQuery
 *
 * @author f0rb on 2019-05-28
 */
@Builder
@QueryTable(table = "permission")
public class PermissionQuery {

   @NestedQueries({
       @NestedQuery(left = "permId", table = "t_role_and_perm", right = "roleId"),
       @NestedQuery(left = "roleId", table = "t_user_and_role", right = "userId"),
   })
   private Integer userId;

}
