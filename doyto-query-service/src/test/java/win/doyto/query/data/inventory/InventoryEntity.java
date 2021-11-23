package win.doyto.query.data.inventory;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import win.doyto.query.entity.Persistable;

import javax.persistence.Table;

/**
 * InventoryEntity
 *
 * @author f0rb on 2021-11-23
 */
@Getter
@Setter
@Table(catalog = "doyto", name = "c_inventory")
public class InventoryEntity implements Persistable<ObjectId> {
    @JsonAlias("_id")
    private ObjectId id;

    private String item;
    private Integer qty;
    private InventorySize size;
    private String status;

}
