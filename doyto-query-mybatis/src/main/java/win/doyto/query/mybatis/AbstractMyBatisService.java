package win.doyto.query.mybatis;

import win.doyto.query.core.AbstractService;

import java.util.List;

/**
 * AbstractMyBatisService
 *
 * @author f0rb
 * @date 2019-05-14
 */
public abstract class AbstractMyBatisService<E, Q> extends AbstractService<E, Q> {

    protected QueryMapper<E, Q> queryMapper;

    public AbstractMyBatisService(QueryMapper<E, Q> queryMapper) {
        this.queryMapper = queryMapper;
    }

    @Override
    public List<E> query(Q query) {
        return queryMapper.query(query);
    }

    @Override
    public long count(Q query) {
        return queryMapper.count(query);
    }

}
