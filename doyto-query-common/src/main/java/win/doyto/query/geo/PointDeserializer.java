package win.doyto.query.geo;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;

/**
 * PointDeserializer
 *
 * @author f0rb on 2021-12-05
 */
public class PointDeserializer extends JsonDeserializer<Point> {
    @Override
    public Point deserialize(JsonParser p, DeserializationContext context) throws IOException {
        TreeNode treeNode = p.readValueAsTree();
        double x;
        double y;
        if (treeNode.isArray()) {
            ArrayNode arrayNode = ((ArrayNode) treeNode);
            x = arrayNode.get(0).doubleValue();
            y = arrayNode.get(1).doubleValue();
        } else {
            JsonNode jsonNode = ((JsonNode) treeNode);
            x = jsonNode.get("x").doubleValue();
            y = jsonNode.get("y").doubleValue();
        }
        return new Point(x, y);
    }
}