package win.doyto.query.mongodb.test.geo;

import lombok.Getter;
import lombok.Setter;
import org.bson.conversions.Bson;
import win.doyto.query.geo.Box;
import win.doyto.query.geo.Circle;
import win.doyto.query.geo.Near;
import win.doyto.query.geo.NearSphere;

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
    private Circle locCenter;
    private Circle locCenterSphere;
    private Box locBox;
    private Bson locBsonBox;
}
