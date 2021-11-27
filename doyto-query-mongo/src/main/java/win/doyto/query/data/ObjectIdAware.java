package win.doyto.query.data;

import org.bson.types.ObjectId;

/**
 * ObjectIdAware
 *
 * @author f0rb on 2021-11-27
 */
public interface ObjectIdAware {
    void setObjectId(ObjectId objectId);
}
