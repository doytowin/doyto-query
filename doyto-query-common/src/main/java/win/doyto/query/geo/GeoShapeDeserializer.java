/*
 * Copyright © 2019-2025 DoytoWin, Inc.
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
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * GeoShapeDeserializer
 *
 * @author f0rb on 2021-12-16
 */
public class GeoShapeDeserializer extends JsonDeserializer<GeoShape<?>> {

    private static final Map<String, Function<JsonNode, GeoShape<?>>> transformMap;

    static  {
        transformMap = new HashMap<>();
        transformMap.put(GeoType.POINT, GeoShapeDeserializer::resolveGeoPoint);
        transformMap.put(GeoType.LINE, GeoShapeDeserializer::resolveGeoLine);
        transformMap.put("LINESTRING", GeoShapeDeserializer::resolveGeoMultiLine);
        transformMap.put(GeoType.POLYGON, GeoShapeDeserializer::resolveGeoPolygon);
        transformMap.put(GeoType.MULTI_POINT.toUpperCase(), GeoShapeDeserializer::resolveGeoMultiPoint);
        transformMap.put(GeoType.MULTI_LINE.toUpperCase(), GeoShapeDeserializer::resolveGeoMultiLine);
        transformMap.put("MULTILINESTRING", GeoShapeDeserializer::resolveGeoMultiLine);
        transformMap.put(GeoType.MULTI_POLYGON.toUpperCase(), GeoShapeDeserializer::resolveGeoMultiPolygon);
        transformMap.put(GeoType.GEOMETRY_COLLECTION.toUpperCase(), GeoShapeDeserializer::resolveGeoCollection);
    }

    @Override
    public GeoShape<?> deserialize(JsonParser p, DeserializationContext context) throws IOException {
        JsonNode treeNode = p.readValueAsTree();
        return resolveGeoShape(treeNode);
    }

    private static GeoShape<?> resolveGeoShape(JsonNode treeNode) {
        JsonNode coordinates = treeNode.get("geometries");
        if (coordinates == null) {
            coordinates = treeNode.get("coordinates");
        }
        String type = treeNode.get("type").asText().toUpperCase();
        return transformMap.get(type).apply(coordinates);
    }

    private static GeoPoint resolveGeoPoint(JsonNode coordinates) {
        return new GeoPoint(PointDeserializer.resolvePoint(coordinates));
    }

    private static GeoMultiPoint resolveGeoMultiPoint(JsonNode coordinates) {
        return new GeoMultiPoint(transform(coordinates));
    }

    private static GeoLine resolveGeoLine(JsonNode coordinates) {
        return new GeoLine(transform(coordinates));
    }

    private static GeoMultiLine resolveGeoMultiLine(JsonNode coordinates) {
        List<List<Point>> lines = new ArrayList<>(coordinates.size());
        for (JsonNode line: coordinates) {
            lines.add(transform(line));
        }
        return new GeoMultiLine(lines);
    }

    private static List<Point> transform(JsonNode coordinates) {
        List<Point> line = new ArrayList<>();
        coordinates.forEach(jsonNode -> line.add(PointDeserializer.resolvePoint(jsonNode)));
        return line;
    }

    private static GeoPolygon resolveGeoPolygon(JsonNode coordinates) {
        List<List<Point>> polygons = new ArrayList<>(coordinates.size());
        for (JsonNode polygon : coordinates) {
            polygons.add(transform(polygon));
        }
        return new GeoPolygon(polygons);
    }

    private static GeoMultiPolygon resolveGeoMultiPolygon(JsonNode coordinates) {
        List<List<List<Point>>> multiPolygon = new ArrayList<>(coordinates.size());
        for (JsonNode polygonNode : coordinates) {
            List<List<Point>> polygons = new ArrayList<>(polygonNode.size());
            for (JsonNode polygon : polygonNode) {
                polygons.add(transform(polygon));
            }
            multiPolygon.add(polygons);
        }
        return new GeoMultiPolygon(multiPolygon);
    }

    private static GeoShape<?> resolveGeoCollection(JsonNode jsonNode) {
        List<GeoShape<?>> geoShapes = new ArrayList<>();
        for (JsonNode geoNode : jsonNode) {
            geoShapes.add(resolveGeoShape(geoNode));
        }
        return new GeoCollection(geoShapes);
    }

}
