package win.doyto.query.core;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DynamicQuery
 *
 * @author f0rb on 2019-05-23
 */
@Getter
@Setter
@Builder
@QueryTable(table = "t_dynamic_${user}_${project}")
public class DynamicQuery {

    private String user;

    private String project;

    private Integer scoreLt;

}
