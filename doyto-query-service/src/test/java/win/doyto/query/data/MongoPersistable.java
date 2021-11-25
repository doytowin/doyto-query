package win.doyto.query.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bson.types.ObjectId;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;

/**
 * AbstractMongoPersistable
 *
 * @author f0rb on 2021-11-23
 */
@Getter
@Setter
public abstract class MongoPersistable<I extends Serializable> implements Persistable<I> {
    private static final long serialVersionUID = -2964571398486901301L;

    @JsonSerialize(using = ToStringSerializer.class)
    private I id;

    @JsonProperty("_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId oid;

    public void setOId(Document objectId) {
        String $oid = objectId.get("$oid", String.class);
        this.setObjectId(new ObjectId($oid));
    }

    public void setObjectId(ObjectId objectId) {
        if (this.id == null) {
            this.id = (I) objectId.toHexString();
        }
    }
}
