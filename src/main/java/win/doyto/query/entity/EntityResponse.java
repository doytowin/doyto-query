package win.doyto.query.entity;

import java.io.Serializable;

/**
 * EntityResponse
 *
 * @author f0rb on 2019-05-26
 */
public interface EntityResponse<E, V extends EntityResponse<E, V>> extends Serializable, Cloneable {

    V buildBy(E e);

}
