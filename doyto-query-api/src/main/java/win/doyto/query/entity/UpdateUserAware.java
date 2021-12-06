package win.doyto.query.entity;

import java.io.Serializable;

/**
 * UpdateUserAware
 *
 * @author f0rb
 */
public interface UpdateUserAware<I extends Serializable> {

    void setUpdateUserId(I updateUser);

}
