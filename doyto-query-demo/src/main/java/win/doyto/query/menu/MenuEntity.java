package win.doyto.query.menu;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
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
class MenuEntity {
    public static final String TABLE = "menu";

    @Id
    @GeneratedValue
    protected Integer id;

}
