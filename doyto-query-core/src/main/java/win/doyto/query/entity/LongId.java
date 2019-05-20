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
public abstract class LongId implements Persistable<Long>, Serializable {

    @Id
    @GeneratedValue
    protected Long id;

}