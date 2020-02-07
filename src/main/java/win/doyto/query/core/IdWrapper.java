package win.doyto.query.core;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

public interface IdWrapper<I extends Serializable> {
    I getId();

    default String toCacheKey() {
        return String.valueOf(getId());
    }

    static <T extends Serializable> IdWrapper.Simple<T> build(T id) {
        return new Simple<>(id);
    }

    @EqualsAndHashCode
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    final class Simple<I extends Serializable> implements IdWrapper<I>{
        private I id;
        @Override
        public I getId() {
            return id;
        }
    }
}
