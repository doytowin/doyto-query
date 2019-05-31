package win.doyto.query.core;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.entity.IntegerId;

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
public class DynamicEntity extends IntegerId {
    public static final String TABLE = "t_dynamic_${user}_${project}";

    @Transient
    private String user;

    @Transient
    private String project;

    @Column(name = "user_score")
    private Integer score;

    private String memo;

    @Transient
    private Date createTime;

}
