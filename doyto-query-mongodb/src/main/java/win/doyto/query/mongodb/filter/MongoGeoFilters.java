package win.doyto.query.mongodb.filter;

import com.mongodb.client.model.Filters;
import lombok.experimental.UtilityClass;
import org.bson.conversions.Bson;
import win.doyto.query.geo.Near;
import win.doyto.query.geo.NearSphere;

/**
 * MongoQuerySuffix
 *
 * @author f0rb on 2021-11-30
 */
@SuppressWarnings("java:S115")
@UtilityClass
public class MongoGeoFilters {

    public static Bson near(String column, Object value)  {
        Near near = (Near) value;
        if (value instanceof NearSphere) {
            return nearSphere(column, value);
        } else {
            return Filters.near(column, near.getX(), near.getY(), near.getMaxDistance(), near.getMinDistance());
        }
    }

    public static Bson nearSphere(String column, Object value)  {
        Near near = (Near) value;
        return Filters.nearSphere(column, near.getX(), near.getY(), near.getMaxDistance(), near.getMinDistance());
    }

}
