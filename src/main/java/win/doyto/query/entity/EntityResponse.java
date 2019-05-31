package win.doyto.query.entity;

import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;

/**
 * EntityResponse
 *
 * @author f0rb on 2019-05-26
 */
public interface EntityResponse<E, V extends EntityResponse<E, V>> extends Serializable, Cloneable {

    @SuppressWarnings("unchecked")
    @SneakyThrows
    default V from(E e) {
        V clone = (V) this.getClass().getConstructor().newInstance();
        BeanUtils.copyProperties(e, clone);
        return clone;
    }
}
