package win.doyto.query.demo.module.menu;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.entity.CommonEntity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * MenuEntity
 *
 * @author f0rb
 */
@Getter
@Setter
@Entity
@Table(name = MenuEntity.TABLE)
class MenuEntity extends CommonEntity<Integer, Long> {

    public static final String TABLE = "menu_${platform}";

    @Transient
    private String platform;

    private Integer parentId;

    private String menuName;

    private String memo;

    private Boolean valid;

}
