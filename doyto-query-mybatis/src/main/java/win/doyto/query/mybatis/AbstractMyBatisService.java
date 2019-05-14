package win.doyto.query.mybatis;

import win.doyto.query.core.AbstractService;

import java.io.Serializable;

/**
 * AbstractMyBatisService
 *
 * @author f0rb
 * @date 2019-05-14
 */
public abstract class AbstractMyBatisService<E, I extends Serializable, Q> extends AbstractService<E, I, Q> {

    public AbstractMyBatisService(QueryMapper<E, I, Q> queryMapper) {
        super(queryMapper);
    }

}
