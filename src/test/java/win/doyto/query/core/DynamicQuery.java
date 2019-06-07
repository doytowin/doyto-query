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
public class DynamicQuery extends PageQuery {

    private String user;

    private String project;

    private Integer scoreLt;

}
