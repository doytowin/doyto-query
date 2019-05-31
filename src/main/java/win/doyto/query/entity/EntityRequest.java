package win.doyto.query.entity;

import org.springframework.beans.BeanUtils;

/**
 * EntityRequest
 *
 * @author f0rb on 2019-05-26
 */
public interface EntityRequest<E> {
    E toEntity();

    default E toEntity(E e) {
        BeanUtils.copyProperties(this, e);
        return e;
    }
}
