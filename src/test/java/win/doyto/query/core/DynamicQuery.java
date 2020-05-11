package win.doyto.query.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.persistence.Transient;

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

    @Transient
    private String user;

    @Transient
    private String project;

    private Integer scoreLt;

    @Override
    protected IdWrapper toIdWrapper() {
        return new DynamicIdWrapper(null, user, project);
    }
}
