package win.doyto.query.mongodb.entity;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

import java.io.IOException;

/**
 * BsonDeserializer
 *
 * @author f0rb on 2021-12-04
 */
public class BsonDeserializer extends JsonDeserializer<Bson> {

    @Override
    public Bson deserialize(JsonParser p, DeserializationContext context) throws IOException {
        TreeNode treeNode = p.readValueAsTree();
        return BsonDocument.parse(treeNode.toString());
    }
}