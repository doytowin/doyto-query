package win.doyto.query.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bson.types.ObjectId;
import win.doyto.query.entity.Persistable;
import win.doyto.query.util.BeanUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.persistence.GeneratedValue;

/**
 * AbstractMongoPersistable
 *
 * @author f0rb on 2021-11-23
 */
@Getter
@Setter
@SuppressWarnings("unchecked")
public abstract class MongoPersistable<I extends Serializable> implements Persistable<I>, ObjectIdAware {

    static Map<Class<?>, Function<ObjectId, ?>> classFuncMap = new HashMap<>();

    @GeneratedValue
    private I id;

    public MongoPersistable() {
        classFuncMap.computeIfAbsent(this.getClass(), clazz -> {
            Class<I> idType = (Class<I>) BeanUtil.getActualTypeArguments(clazz)[0];
            Function<ObjectId, ?> setIdFunc;
            if (idType.isAssignableFrom(String.class)) {
                setIdFunc = ObjectId::toHexString;
            } else if (idType.isAssignableFrom(ObjectId.class)) {
                setIdFunc = objectId -> objectId;
            } else {
                setIdFunc = objectId -> null;
            }
            return setIdFunc;
        });
    }

    @JsonProperty("_id")
    private ObjectId objectId;

    @JsonSetter("_id")
    public void setOid(Document objectId) {
        if (objectId != null) {
            String $oid = objectId.get("$oid", String.class);
            this.setObjectId(new ObjectId($oid));
        }
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
        if (this.id == null) {
            this.id = (I) classFuncMap.get(this.getClass()).apply(objectId);
        }
    }
}
