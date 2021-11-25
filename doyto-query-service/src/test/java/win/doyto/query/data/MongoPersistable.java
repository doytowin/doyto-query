package win.doyto.query.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bson.types.ObjectId;
import win.doyto.query.entity.Persistable;
import win.doyto.query.util.BeanUtil;

import java.io.Serializable;
import javax.persistence.GeneratedValue;

/**
 * AbstractMongoPersistable
 *
 * @author f0rb on 2021-11-23
 */
@Getter
@Setter
@SuppressWarnings("unchecked")
public abstract class MongoPersistable<I extends Serializable> implements Persistable<I> {

    @GeneratedValue
    private I id;

    @GeneratedValue
    private Class<I> idType;

    public MongoPersistable() {
        this.idType = (Class<I>) BeanUtil.getActualTypeArguments(this.getClass())[0];
    }

    @JsonProperty("_id")
    private ObjectId oid;

    public void setOId(Document objectId) {
        if (objectId != null) {
            String $oid = objectId.get("$oid", String.class);
            this.setObjectId(new ObjectId($oid));
        }
    }

    public void setObjectId(ObjectId objectId) {
        this.oid = objectId;
        if (this.id == null) {
            if (idType.isAssignableFrom(String.class)) {
                this.id = (I) objectId.toHexString();
            } else if( idType.isAssignableFrom(ObjectId.class)) {
                this.id = (I) objectId;
            }
        }
    }
}
