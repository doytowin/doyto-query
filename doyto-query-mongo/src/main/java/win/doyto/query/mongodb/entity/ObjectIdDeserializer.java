package win.doyto.query.mongodb.entity;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.bson.types.ObjectId;

import java.io.IOException;

/**
 * ObjectIdDeserializer
 *
 * @author f0rb on 2021-11-26
 */
public class ObjectIdDeserializer extends JsonDeserializer<ObjectId> {

    @Override
    public ObjectId deserialize(JsonParser p, DeserializationContext context) throws IOException {
        TreeNode treeNode = p.readValueAsTree();
        JsonNode oid = ((JsonNode) treeNode).get("$oid");
        return new ObjectId(oid.asText());
    }
}