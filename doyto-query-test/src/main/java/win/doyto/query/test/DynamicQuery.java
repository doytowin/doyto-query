package win.doyto.query.test;

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
public class DynamicQuery extends TestPageQuery {

    @Transient
    private String user;

    @Transient
    private String project;

    @Transient
    private String locale;

    private Integer scoreLt;

    @Override
    public DynamicIdWrapper toIdWrapper() {
        return new DynamicIdWrapper(null, user, project, locale);
    }
}
