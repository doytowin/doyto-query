package win.doyto.query.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * CreateUserAware
 *
 * @author f0rb
 * @date 2019-05-17
 */
public interface CreateUserAware<I extends Serializable> {

    void setCreateUserId(I createUser);

    void setCreateTime(Date createTime);

}
