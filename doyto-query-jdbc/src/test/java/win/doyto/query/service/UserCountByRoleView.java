package win.doyto.query.service;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.annotation.Joins;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * UserCountByRoleView
 *
 * @author f0rb on 2019-06-15
 */
@Getter
@Setter
@Table(name = "user u")
@Joins(value = {
    @Joins.Join("left join t_user_and_role ur on ur.userId = u.id"),
    @Joins.Join("inner join role r on r.id = ur.roleId")
}, groupBy = "r.roleName", having = "count(*) > 0")
public class UserCountByRoleView {

    @Column(name = "r.roleName")
    private String roleName;

    @Column(name = "count(u.id)")
    private Integer userCount;

}
