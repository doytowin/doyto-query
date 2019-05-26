package win.doyto.query.test.user;

import lombok.*;
import win.doyto.query.core.PageQuery;
import win.doyto.query.core.QueryField;
import win.doyto.query.core.QueryTable;

/**
 * UserQuery
 *
 * @author f0rb
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@QueryTable(table = UserEntity.TABLE, entityClass = UserEntity.class)
public class UserQuery extends PageQuery {
    private String username;

    @QueryField(and = "(username = #{account} OR email = #{account} OR mobile = #{account})")
    private String account;
}
