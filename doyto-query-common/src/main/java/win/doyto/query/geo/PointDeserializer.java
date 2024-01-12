/*
 * Copyright © 2019-2024 Forb Yuan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        return resolvePoint(treeNode);
    }

    static Point resolvePoint(TreeNode treeNode) {
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