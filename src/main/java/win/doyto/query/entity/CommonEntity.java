package win.doyto.query.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * CommonEntity
 *
 * @author f0rb
 */
@Getter
@Setter
@MappedSuperclass
public abstract class CommonEntity<I extends Serializable, U extends Serializable>
    implements Persistable<I>, Serializable, CreateUserAware<U>, UpdateUserAware<U> {

    @Id
    @GeneratedValue
    protected I id;

    /**
     * 创建者
     */
    private U createUserId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新者
     */
    private U updateUserId;

    /**
     * 更新时间
     */
    private Date updateTime;

}
