/*
 * Copyright © 2019-2022 Forb Yuan
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

package win.doyto.query.mongodb.filter;

import com.mongodb.client.model.geojson.*;
import lombok.experimental.UtilityClass;
import win.doyto.query.geo.Point;
import win.doyto.query.geo.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * GeoTransformer
 *
 * @author f0rb on 2022-03-09
 */
@UtilityClass
public class GeoTransformer {

    private static final Map<Class<?>, Function<Object, Geometry>> transFuncMap = new HashMap<>();

    static {
        transFuncMap.put(GeoPoint.class, geo -> transform((GeoPoint) geo));
        transFuncMap.put(GeoLine.class, geo -> transform((GeoLine) geo));
        transFuncMap.put(GeoPolygon.class, geo -> transform((GeoPolygon) geo));
        transFuncMap.put(GeoMultiPoint.class, geo -> transform((GeoMultiPoint) geo));
        transFuncMap.put(GeoMultiLine.class, geo -> transform((GeoMultiLine) geo));
        transFuncMap.put(GeoMultiPolygon.class, geo -> transform((GeoMultiPolygon) geo));
        transFuncMap.put(GeoCollection.class, geo -> transform((GeoCollection) geo));
    }

    private static Geometry transform(GeoPoint geo) {
        Position coordinate = new Position(geo.getCoordinates().toList());
        return new com.mongodb.client.model.geojson.Point(coordinate);
    }

    private static Geometry transform(GeoMultiPoint geo) {
        List<Position> coordinates = buildPositions(geo.getCoordinates());
        return new MultiPoint(coordinates);
    }

    private static Geometry transform(GeoLine geo) {
        List<Position> coordinates = buildPositions(geo.getCoordinates());
        return new LineString(coordinates);
    }

    private static Geometry transform(GeoMultiLine geo) {
        List<List<Point>> coordinates = geo.getCoordinates();
        List<List<Position>> lines = new LinkedList<>();
        coordinates.forEach(polygon -> lines.add(buildPositions(polygon)));
        return new MultiLineString(lines);
    }

    private static List<Position> buildPositions(List<Point> coordinates) {
        return coordinates.stream()
                          .map(point -> new Position(point.toList()))
                          .collect(Collectors.toList());
    }

    private static Geometry transform(GeoPolygon geo) {
        List<List<Point>> coordinates = geo.getCoordinates();
        return new Polygon(buildPolygonCoordinates(coordinates));
    }

    private static PolygonCoordinates buildPolygonCoordinates(List<List<Point>> coordinates) {
        List<List<Position>> holes = new LinkedList<>();
        coordinates.forEach(polygon -> {
            connectToRing(polygon);
            holes.add(buildPositions(polygon));
        });
        return new PolygonCoordinates(holes.remove(0), holes);
    }

    private static void connectToRing(List<Point> polygon) {
        if (!polygon.get(polygon.size() - 1).equals(polygon.get(0))) {
            polygon.add(polygon.get(0));
        }
    }

    private static Geometry transform(GeoMultiPolygon geo) {
        List<List<List<Point>>> coordinates = geo.getCoordinates();
        List<PolygonCoordinates> polygons =
                coordinates.stream()
                           .map(GeoTransformer::buildPolygonCoordinates)
                           .collect(Collectors.toList());
        return new MultiPolygon(polygons);
    }

    private static Geometry transform(GeoCollection geo) {
        List<GeoShape<?>> geoShapes = geo.getCoordinates();
        List<Geometry> geometries = geoShapes.stream().map(GeoTransformer::transform).collect(Collectors.toList());
        return new GeometryCollection(geometries);
    }

    static boolean support(Object value) {
        return transFuncMap.containsKey(value.getClass());
    }

    static Geometry transform(Object value) {
        return transFuncMap.get(value.getClass()).apply(value);
    }
}
