package win.doyto.query.user;

import lombok.*;
import win.doyto.query.core.PageQuery;
import win.doyto.query.core.QueryField;
import win.doyto.query.core.QueryTable;

/**
 * UserQuery
 *
 * @author f0rb
 * @date 2019-05-12
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@QueryTable(table = "user")
public class UserQuery extends PageQuery {
    private String username;

    @QueryField(and = "(username = #{account} OR email = #{account} OR mobile = #{account})")
    private String account;
}
