package win.doyto.query.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * IntegerId
 *
 * @author f0rb
 */
@Getter
@Setter
@MappedSuperclass
public abstract class IntegerId implements Persistable<Integer>, Serializable {

    @Id
    @GeneratedValue
    protected Integer id;

}