package win.doyto.query.service;

import win.doyto.query.core.test.TestEntity;
import win.doyto.query.core.test.TestQuery;

/**
 * TestService
 *
 * @author f0rb
 */
public class TestService extends AbstractCrudService<TestEntity, Integer, TestQuery> {
    @Override
    protected String getCacheName() {
        return "module:user";
    }
}
