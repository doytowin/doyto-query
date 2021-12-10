package win.doyto.query.util.menu;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.entity.AbstractPersistable;

import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * MenuEntity
 *
 * @author f0rb
 */
@Getter
@Setter
@Entity
@Table(name = "menu${platform}")
public class MenuEntity extends AbstractPersistable<Integer> {

    private String platform;

    private Integer parentId;

    private String menuName;

    private String memo;

    private Boolean valid;

    @Override
    public MenuIdWrapper toIdWrapper() {
        Objects.requireNonNull(platform);
        return new MenuIdWrapper(id, platform);
    }
}
