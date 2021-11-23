package win.doyto.query.data.inventory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SizeEntity
 *
 * @author f0rb on 2021-11-23
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventorySize {
    private Double h;
    private Double w;
    private String uom;
}
