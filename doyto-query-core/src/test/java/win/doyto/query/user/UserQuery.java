package win.doyto.query.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import win.doyto.query.core.PageQuery;
import win.doyto.query.core.QueryField;
import win.doyto.query.core.QueryTable;

import java.util.Date;
import java.util.List;

/**
 * UserQuery
 *
 * @author f0rb
 * @date 2019-05-12
 */
@Getter
@Setter
@Builder
@QueryTable(table = "user", entityClass = UserEntity.class)
public class UserQuery extends PageQuery {
    private List<Integer> idIn;
    private List<Integer> idNotIn;
    private Integer idLt;
    private Integer idLe;

    private String username;

    @QueryField(and = "(username = #{account} OR email = #{account} OR mobile = #{account})")
    private String account;

    private String usernameOrEmailOrMobile;

    private String usernameOrEmailOrMobileLike;

    private String usernameLike;

    private Date createTimeGt;
    private Date createTimeGe;
    private Date createTimeLt;
    private Date createTimeLe;
}
