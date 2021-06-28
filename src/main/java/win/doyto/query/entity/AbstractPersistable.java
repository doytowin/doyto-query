package win.doyto.query.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * AbstractId
 *
 * @author f0rb on 2021-06-27
 */
@Getter
@Setter
public abstract class AbstractPersistable<I extends Serializable> implements Persistable<I>, Serializable {
    private static final long serialVersionUID = -4538555675455803732L;
    @Id
    @GeneratedValue
    protected I id;
}
