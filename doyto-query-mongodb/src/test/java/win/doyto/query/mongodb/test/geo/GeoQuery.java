package win.doyto.query.mongodb.test.geo;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.mongodb.model.Near;
import win.doyto.query.mongodb.model.NearSphere;

/**
 * GeoQuery
 *
 * @author f0rb on 2021-11-30
 */
@Getter
@Setter
public class GeoQuery {
    private Near locNear;
    private Near locNearSphere;
    private NearSphere loc2Near;
}
