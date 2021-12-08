package win.doyto.query.mongodb.test.inventory;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.entity.MongoEntity;
import win.doyto.query.mongodb.entity.MongoPersistable;

/**
 * InventoryEntity
 *
 * @author f0rb on 2021-11-23
 */
@Getter
@Setter
@MongoEntity(database = "doyto", collection = "c_inventory")
public class InventoryEntity extends MongoPersistable<String> {

    private String item;
    private Integer qty;
    private InventorySize size;
    private String status;

}
