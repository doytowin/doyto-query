package win.doyto.query.user;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * UserEntity
 *
 * @author f0rb
 * @date 2019-05-12
 */
@Getter
@Setter
@Entity
@Table(name = "user")
@EqualsAndHashCode(callSuper = true)
class UserEntity extends AbstractPersistable<Integer> {
    private String username;
    private String password;
    private String mobile;
    private String email;
    private String nickname;
    private String valid;
}