package win.doyto.query.mybatis;

import lombok.extern.slf4j.Slf4j;
import win.doyto.query.core.AbstractMockDataAccess;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;

/**
 * AbstractMockMapper
 *
 * @author f0rb
 * @date 2019-05-15
 */
@Slf4j
public abstract class AbstractMockMapper<E extends Persistable<I>, I extends Serializable, Q> extends AbstractMockDataAccess<E, I, Q> {

    public AbstractMockMapper(String table) {
        super(table);
    }

}
