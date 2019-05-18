package win.doyto.query.module.menu;

import win.doyto.query.mybatis.AbstractMockMapper;

/**
 * MockMenuMapper
 *
 * @author f0rb
 * @date 2019-05-15
 */
public class MockMenuMapper extends AbstractMockMapper<MenuEntity, Integer, MenuQuery> implements MenuMapper {
    public MockMenuMapper() {
        super(MenuEntity.TABLE);
    }
}
