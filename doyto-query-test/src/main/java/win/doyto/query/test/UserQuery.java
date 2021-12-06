package win.doyto.query.test;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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
public class UserQuery extends TestPageQuery {
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
