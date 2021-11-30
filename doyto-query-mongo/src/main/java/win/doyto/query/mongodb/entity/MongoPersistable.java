package win.doyto.query.mongodb.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;
import javax.persistence.GeneratedValue;

/**
 * AbstractMongoPersistable
 *
 * @author f0rb on 2021-11-23
 */
@Getter
@Setter
public abstract class MongoPersistable<I extends Serializable> implements Persistable<I>, ObjectIdAware {

    @GeneratedValue
    private I id;

    protected MongoPersistable() {
        ObjectIdMapper.initIdMapper(this.getClass());
    }

    @JsonProperty("_id")
    @JsonSerialize(using = ObjectIdSerializer.class)
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    private ObjectId objectId;

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
        if (this.id == null) {
            this.id = ObjectIdMapper.convert(this.getClass(), objectId);
        }
    }
}
