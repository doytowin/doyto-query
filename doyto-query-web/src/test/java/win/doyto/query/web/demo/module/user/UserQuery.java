package win.doyto.query.web.demo.module.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import win.doyto.query.core.PageQuery;

/**
 * UserQuery
 *
 * @author f0rb on 2020-04-01
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserQuery extends PageQuery {
    private String username;
    private String email;
    private String mobile;
    private String usernameOrEmailOrMobile;
    private String usernameLike;
    private String emailLike;
    private boolean memoNull;
    private UserLevel userLevel;

    public void setAccount(String account) {
        this.usernameOrEmailOrMobile = account;
    }
}
