package win.doyto.query.data.inventory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import win.doyto.query.core.Query;

/**
 * SizeQuery
 *
 * @author f0rb on 2021-11-24
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SizeQuery implements Query {
    private Integer hLt;
}
