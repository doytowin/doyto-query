package win.doyto.query.core;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * UserQuery
 *
 * @author f0rb
 * @date 2019-05-12
 */
@Getter
@Setter
@Builder
@QueryTable(table = "user")
public class UserQuery extends PageQuery {
    private String username;

    @QueryField(and = "(username = #{account} OR email = #{account} OR mobile = #{account})")
    private String account;
}
