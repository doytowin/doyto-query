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

import com.mongodb.client.model.geojson.Geometry;
import com.mongodb.client.model.geojson.LineString;
import com.mongodb.client.model.geojson.Position;
import lombok.experimental.UtilityClass;
import win.doyto.query.geo.GeoLine;
import win.doyto.query.geo.GeoPoint;

import java.util.HashMap;
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
    }

    private static Geometry transform(GeoPoint geo) {
        Position coordinate = new Position(geo.getCoordinates().toList());
        return new com.mongodb.client.model.geojson.Point(coordinate);
    }

    private static Geometry transform(GeoLine geo) {
        List<Position> coordinates =
                geo.getCoordinates().stream().map(point -> new Position(point.toList())).collect(Collectors.toList());
        return new LineString(coordinates);
    }

    static boolean support(Object value) {
        return transFuncMap.containsKey(value.getClass());
    }

    static Geometry transform(Object value) {
        return transFuncMap.get(value.getClass()).apply(value);
    }
}
