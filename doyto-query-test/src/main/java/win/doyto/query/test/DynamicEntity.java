package win.doyto.query.test;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.core.IdWrapper;
import win.doyto.query.entity.AbstractPersistable;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * DynamicEntity
 *
 * @author f0rb on 2019-05-25
 */
@Getter
@Setter
@Entity
@Table(name = DynamicEntity.TABLE)
public class DynamicEntity extends AbstractPersistable<Integer> {
    public static final String TABLE = "t_dynamic_${user}_${project}";

    @Transient
    private String user;

    @Transient
    private String project;

    @Transient
    private String locale;

    @Column(name = "locale_${locale}")
    private String value;

    @Column(name = "user_score")
    private Integer score;

    private String memo;

    @Transient
    private Date createTime;

    @Override
    public IdWrapper<Integer> toIdWrapper() {
        return new DynamicIdWrapper(id, user, project, locale);
    }
}
