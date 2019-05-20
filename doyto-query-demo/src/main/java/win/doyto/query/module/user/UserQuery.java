package win.doyto.query.module.user;

import lombok.*;
import win.doyto.query.core.PageQuery;
import win.doyto.query.core.QueryTable;
import win.doyto.query.util.ColumnUtil;

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
@SuppressWarnings("unused")
public class UserQuery extends PageQuery {
    private String username;

    private String usernameOrEmailOrMobile;

    private String mobile;

    private String usernameLike;

    public String getUsernameLike() {
        return ColumnUtil.escapeLike(usernameLike);
    }

    public void setAccount(String account) {
        this.usernameOrEmailOrMobile = account;
    }
}
