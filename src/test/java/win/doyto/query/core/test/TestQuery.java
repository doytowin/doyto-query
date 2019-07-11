package win.doyto.query.core.test;

import lombok.*;
import win.doyto.query.annotation.QueryField;
import win.doyto.query.annotation.SubQuery;
import win.doyto.query.core.PageQuery;

import java.util.Date;
import java.util.List;

/**
 * TestQuery
 *
 * @author f0rb
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestQuery extends PageQuery {
    private List<Integer> idIn;
    private List<Integer> idNotIn;
    private Integer idLt;
    private Integer idLe;

    private String username;

    @SubQuery(left = "userId", table = "t_user_and_role")
    private Integer roleId;

    @QueryField(and = "(username = ? OR email = ? OR mobile = ?)")
    private String account;

    private String email;

    private String usernameOrEmailOrMobile;

    private String usernameOrEmailOrMobileLike;

    private String usernameLike;
    private String usernameStart;

    private String userNameOrUserCodeLike;

    private boolean memoNull;
    private boolean memoNotNull;

    private TestEnum userLevel;

    private TestEnum userLevelNot;
    private List<TestEnum> userLevelIn;

    private List<TestStringEnum> statusIn;

    private Date createTimeGt;
    private Date createTimeGe;
    private Date createTimeLt;
    private Date createTimeLe;

    private Boolean valid;

}
