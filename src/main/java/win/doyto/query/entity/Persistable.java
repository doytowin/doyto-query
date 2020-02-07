package win.doyto.query.entity;

import win.doyto.query.core.IdWrapper;

import java.beans.Transient;
import java.io.Serializable;

/**
 * Persistable
 *
 * @author f0rb
 */
public interface Persistable<I extends Serializable> extends Serializable {
    I getId();

    void setId(I id);

    @Transient
    default boolean isNew() {
        return getId() == null;
    }

    default IdWrapper<I> toIdWrapper() {
        return IdWrapper.build(getId());
    }
}
