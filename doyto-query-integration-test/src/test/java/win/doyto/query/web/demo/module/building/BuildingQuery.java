package win.doyto.query.web.demo.module.building;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import win.doyto.query.core.PageQuery;

import java.math.BigInteger;

/**
 * BuildingQuery
 *
 * @author f0rb on 2021-12-06
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BuildingQuery extends PageQuery {
    private BigInteger id;
}
