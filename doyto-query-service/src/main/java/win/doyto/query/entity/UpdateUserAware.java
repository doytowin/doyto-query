package win.doyto.query.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * UpdateUserAware
 *
 * @author f0rb
 */
public interface UpdateUserAware<I extends Serializable> {

    void setUpdateUserId(I updateUser);

    void setUpdateTime(Date updateTime);

}
