package win.doyto.query.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * CommonEntity
 *
 * @author f0rb
 */
@Getter
@Setter
public abstract class CommonEntity<I extends Serializable, U extends Serializable>
        extends AbstractPersistable<I> implements Serializable, CreateUserAware<U>, UpdateUserAware<U> {

    private static final long serialVersionUID = 3904043862384245488L;
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
