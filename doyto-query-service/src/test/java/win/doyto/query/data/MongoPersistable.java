package win.doyto.query.data;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bson.types.ObjectId;
import win.doyto.query.entity.Persistable;

/**
 * AbstractMongoPersisterble
 *
 * @author f0rb on 2021-11-23
 */
@Getter
@Setter
public abstract class MongoPersistable implements Persistable<ObjectId> {
    private static final long serialVersionUID = -2964571398486901301L;

    @JsonSerialize(using = ToStringSerializer.class)
    protected ObjectId id;

    @JsonSetter("_id")
    public void setObjectId(Document objectId) {
        this.id = new ObjectId(objectId.get("$oid", String.class));
    }
}
