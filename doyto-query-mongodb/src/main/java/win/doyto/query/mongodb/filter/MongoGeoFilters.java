package win.doyto.query.mongodb.filter;

import com.mongodb.client.model.Filters;
import lombok.experimental.UtilityClass;
import org.bson.conversions.Bson;
import win.doyto.query.geo.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MongoQuerySuffix
 *
 * @author f0rb on 2021-11-30
 */
@SuppressWarnings("java:S115")
@UtilityClass
public class MongoGeoFilters {

    public static Bson near(String column, Object value)  {
        if (value instanceof NearSphere) {
            return nearSphere(column, value);
        } else {
            Near near = (Near) value;
            return Filters.near(column, near.getX(), near.getY(), near.getMaxDistance(), near.getMinDistance());
        }
    }

    public static Bson nearSphere(String column, Object value) {
        Near near = (Near) value;
        return Filters.nearSphere(column, near.getX(), near.getY(), near.getMaxDistance(), near.getMinDistance());
    }

    public static Bson withinCenter(String column, Object value) {
        Circle circle = (Circle) value;
        return Filters.geoWithinCenter(column, circle.getX(), circle.getY(), circle.getRadius());
    }

    public static Bson withinCenterSphere(String column, Object value) {
        Circle circle = (Circle) value;
        return Filters.geoWithinCenterSphere(column, circle.getX(), circle.getY(), circle.getRadius());
    }

    public static Bson withinBox(String column, Object value) {
        Box box = (Box) value;
        Point p1 = box.getP1();
        Point p2 = box.getP2();
        return Filters.geoWithinBox(column, p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    @SuppressWarnings("unchecked")
    public static Bson withinPolygon(String column, Object value) {
        List<List<Double>> points = ((List<Point>) value)
                .stream().map(Point::toList).collect(Collectors.toList());

        return Filters.geoWithinPolygon(column, points);
    }

    public static Bson within(String column, Object value) {
        return Filters.geoWithin(column, (Bson) value);
    }

    public static Bson intersects(String column, Object value) {
        return Filters.geoIntersects(column, (Bson) value);
    }
}
