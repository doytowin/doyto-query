package win.doyto.query.menu;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import win.doyto.query.core.PageList;

import java.util.List;

/**
 * MenuService
 *
 * @author f0rb
 * @date 2019-05-13
 */
@Service
@AllArgsConstructor
class MenuService {

    MenuMapper menuMapper;

    public List<MenuEntity> query(MenuQuery menuQuery) {
        return menuMapper.query(menuQuery);
    }

    public Long count(MenuQuery menuQuery) {
        return menuMapper.count(menuQuery);
    }

    public PageList<MenuEntity> page(MenuQuery menuQuery) {
        return new PageList<>(query(menuQuery), count(menuQuery));
    }

}
