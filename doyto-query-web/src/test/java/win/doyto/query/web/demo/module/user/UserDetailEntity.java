package win.doyto.query.web.demo.module.user;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * UserDetailEntity
 *
 * @author f0rb on 2019-06-26
 */
@Getter
@Setter
@Entity
@Table(name = "user_detail")
public class UserDetailEntity implements Persistable<Long>, Serializable {

    @Id
    protected Long id;

    private String address;

    public static UserDetailEntity build(Long id, UserRequest request) {
        UserDetailEntity userDetailEntity = new UserDetailEntity();
        userDetailEntity.setId(id);
        userDetailEntity.setAddress(request.getAddress());
        return userDetailEntity;
    }
}
