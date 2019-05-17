package win.doyto.query.user;

import lombok.*;
import win.doyto.query.core.PageQuery;
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
@QueryTable(table = UserEntity.TABLE, entityClass = UserEntity.class)
@SuppressWarnings("unused")
public class UserQuery extends PageQuery {
    private String username;

    private String usernameOrEmailOrMobile;

    private String mobile;

    private String usernameLike;

    public String getUsernameLike() {
        return usernameLike == null ? null : "%" + usernameLike + "%";
    }

    public void setAccount(String account) {
        this.usernameOrEmailOrMobile = account;
    }
}
