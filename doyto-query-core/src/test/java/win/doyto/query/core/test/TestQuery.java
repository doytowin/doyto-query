package win.doyto.query.core.test;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import win.doyto.query.annotation.NestedQueries;
import win.doyto.query.annotation.NestedQuery;
import win.doyto.query.annotation.QueryField;
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
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TestQuery extends PageQuery {
    private List<Integer> idIn;
    private List<Integer> idNotIn;
    private Integer idLt;
    private Integer idLe;

    private String username;
    private String usernameEq;

    @NestedQueries(@NestedQuery(select = "userId", from = "t_user_and_role"))
    private Integer roleId;

    @QueryField(and = "(username = ? OR email = ? OR mobile = ?)")
    private String account;

    private String email;

    private String usernameOrEmailOrMobile;

    private String usernameOrEmailOrMobileLike;
    private String usernameLikeOrEmailLikeOrMobileLike;

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
