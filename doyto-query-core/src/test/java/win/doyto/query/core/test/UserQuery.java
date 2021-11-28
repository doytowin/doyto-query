package win.doyto.query.core.test;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import win.doyto.query.core.PageQuery;

/**
 * TestQuery
 *
 * @author f0rb
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("unused")
public class UserQuery extends PageQuery {
    private String username;

    private String usernameOrEmailOrMobile;

    private String mobile;

    private String usernameLike;

    private String emailLike;

    private boolean memoNull;

    private UserLevel userLevel;

    public void setAccount(String account) {
        this.usernameOrEmailOrMobile = account;
    }
}
