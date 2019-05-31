package win.doyto.query.core.module.user;

import lombok.*;
import win.doyto.query.core.NestedQuery;
import win.doyto.query.core.PageQuery;
import win.doyto.query.core.QueryField;
import win.doyto.query.core.QueryTable;

import java.util.Date;
import java.util.List;

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
@QueryTable(table = UserEntity.TABLE)
public class UserQuery extends PageQuery {
    private List<Integer> idIn;
    private List<Integer> idNotIn;
    private Integer idLt;
    private Integer idLe;

    private String username;

    @NestedQuery(left = "userId", table = "t_user_and_role", right = "roleId")
    private Integer roleId;

    @QueryField(and = "(username = ? OR email = ? OR mobile = ?)")
    private String account;

    private String usernameOrEmailOrMobile;

    private String usernameOrEmailOrMobileLike;

    private String usernameLike;

    private String userNameOrUserCodeLike;

    private UserLevel userLevel;

    private Date createTimeGt;
    private Date createTimeGe;
    private Date createTimeLt;
    private Date createTimeLe;

    private Boolean valid;

}
