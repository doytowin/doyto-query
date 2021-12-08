package win.doyto.query.web.demo.test.role;

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
public class RoleQuery extends PageQuery {
    private String roleName;
    private String roleNameLike;
}
