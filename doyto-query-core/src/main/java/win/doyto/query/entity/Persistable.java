package win.doyto.query.entity;

import java.beans.Transient;
import java.io.Serializable;

/**
 * Persistable
 *
 * @author f0rb
 * @date 2019-05-14
 */
public interface Persistable<I> extends Serializable {
    I getId();

    @Transient
    default boolean isNew() {
        return getId() == null;
    }
}
