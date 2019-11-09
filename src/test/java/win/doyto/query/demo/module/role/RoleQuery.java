package win.doyto.query.demo.module.role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import win.doyto.query.annotation.NestedQuery;
import win.doyto.query.core.PageQuery;

/**
 * RoleQuery
 *
 * @author f0rb on 2019-05-28
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RoleQuery extends PageQuery {

    @NestedQuery(left = "roleId", table = "t_user_and_role")
    private Long userId;

}
