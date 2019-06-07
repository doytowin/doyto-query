package win.doyto.query.core.test;

import lombok.Builder;
import win.doyto.query.core.*;

import java.util.List;

/**
 * PermissionQuery
 *
 * @author f0rb on 2019-05-28
 */
@Builder
@QueryTable(table = "permission")
public class PermissionQuery extends PageQuery {

   @NestedQueries(
       value = {
           @NestedQuery(left = "permId", table = "t_role_and_perm"),
           @NestedQuery(left = "roleId", table = "t_user_and_role"),
       })
   private Integer userId;

   @NestedQueries({
       @NestedQuery(left = "permId", table = "t_role_and_perm"),
       @NestedQuery(left = "roleId", table = "t_user_and_role ur",
           extra = "inner join user u on u.id = ur.userId and u.valid = true"
       )
   })
   private boolean validUser;

   @SubQuery(left = "permId", table = "t_role_and_perm")
   private List<Integer> roleIdIn;

}
