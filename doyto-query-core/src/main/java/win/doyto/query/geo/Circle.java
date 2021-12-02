package win.doyto.query.geo;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Circle
 *
 * @author f0rb on 2021-12-01
 */
@Getter
@Setter
public class Circle {
    @NonNull
    private Point center;
    private double radius;

    public double getX() {
        return center.getX();
    }

    public double getY() {
        return center.getY();
    }
}
