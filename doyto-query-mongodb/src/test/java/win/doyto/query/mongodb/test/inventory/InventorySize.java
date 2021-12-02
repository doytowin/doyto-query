package win.doyto.query.mongodb.test.inventory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import win.doyto.query.mongodb.entity.MongoPersistable;

import javax.persistence.Table;

/**
 * SizeEntity
 *
 * @author f0rb on 2021-11-23
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(catalog = "doyto", name = "c_inventory")
public class InventorySize extends MongoPersistable<ObjectId> {
    private Double h;
    private Double w;
    private String uom;
}
