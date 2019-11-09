package win.doyto.query.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * DynamicQuery
 *
 * @author f0rb on 2019-05-23
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DynamicQuery extends PageQuery {

    private String user;

    private String project;

    private Integer scoreLt;

}
