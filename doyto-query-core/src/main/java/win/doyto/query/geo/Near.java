package win.doyto.query.geo;

import lombok.Getter;
import lombok.Setter;

/**
 * GeoNear
 *
 * @author f0rb on 2021-11-30
 */
@Getter
@Setter
public class Near {
    private Point center;
    private Double minDistance;
    private Double maxDistance;

    public double getX() {
        return center.getX();
    }

    public double getY() {
        return center.getY();
    }

}
