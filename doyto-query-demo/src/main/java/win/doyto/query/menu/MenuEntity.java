package win.doyto.query.menu;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.entity.IntegerId;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * MenuEntity
 *
 * @author f0rb
 * @date 2019-05-13
 */
@Getter
@Setter
@Entity
@Table(name = MenuEntity.TABLE)
class MenuEntity extends IntegerId {
    public static final String TABLE = "menu";

    private Integer parentId;

    private String menuName;

    private String memo;

    private Boolean valid;

}
