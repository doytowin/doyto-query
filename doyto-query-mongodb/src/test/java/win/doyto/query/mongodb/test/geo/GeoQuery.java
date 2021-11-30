package win.doyto.query.mongodb.test.geo;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.mongodb.model.Near;

/**
 * GeoQuery
 *
 * @author f0rb on 2021-11-30
 */
@Getter
@Setter
public class GeoQuery {
    private Near locNear;
}
