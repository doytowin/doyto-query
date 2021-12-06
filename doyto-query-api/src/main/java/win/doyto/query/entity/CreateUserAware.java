package win.doyto.query.entity;

import java.io.Serializable;

/**
 * CreateUserAware
 *
 * @author f0rb
 */
public interface CreateUserAware<I extends Serializable> {

    void setCreateUserId(I createUser);

}
