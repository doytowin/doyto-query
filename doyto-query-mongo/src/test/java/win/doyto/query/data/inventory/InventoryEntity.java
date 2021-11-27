package win.doyto.query.data.inventory;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.data.MongoPersistable;

import javax.persistence.Table;

/**
 * InventoryEntity
 *
 * @author f0rb on 2021-11-23
 */
@Getter
@Setter
@Table(catalog = "doyto", name = "c_inventory")
public class InventoryEntity extends MongoPersistable<String> {

    private String item;
    private Integer qty;
    private InventorySize size;
    private String status;

}
