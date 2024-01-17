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

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import win.doyto.query.util.BeanUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GeoTest
 *
 * @author f0rb on 2022-03-15
 */
class GeoTest {

    @Test
    void defineGeoPoint() {
        GeoPoint geoPoint = new GeoPoint(new Point(3, 6));

        assertThat(geoPoint.getType()).isEqualTo("POINT");
        assertThat(geoPoint.getCoordinates().toList()).containsExactly(3.0, 6.0);
    }

    @Test
    void defineGeoLine() {
        List<Point> line = Arrays.asList(new Point(0, 0), new Point(3, 6), new Point(6, 1));
        GeoLine geoLine = new GeoLine(line);

        assertThat(geoLine.getType()).isEqualTo("LINE");
        assertThat(geoLine.getCoordinates()).contains(line.toArray(new Point[0]));
    }

    @Test
    void defineGeoPolygon() {
        //[[[0,0], [3,6], [6,1], [0,0]]]
        List<Point> exterior = Arrays.asList(new Point(0, 0), new Point(3, 6), new Point(6, 1));
        GeoPolygon geoPolygon = new GeoPolygon(Arrays.asList(exterior));

        assertThat(geoPolygon.getType()).isEqualTo("POLYGON");
        assertThat(geoPolygon.getCoordinates()).hasSize(1);
        assertThat(geoPolygon.getCoordinates().get(0))
                .hasSize(3)
                .containsAll(exterior);
    }

    @Test
    void deserializeGeoPolygon() {
        String polygonJson = "{\"type\":\"Polygon\",\"coordinates\":[[[0,0],[3,6],[6,1],[0,0]],[[2,2],[3,3],[4,2],[2,2]]]}";
        GeoPolygon geoPolygon = (GeoPolygon) BeanUtil.parse(polygonJson, GeoShape.class);

        assertThat(geoPolygon.getType()).isEqualTo("POLYGON");
        assertThat(geoPolygon.getCoordinates()).hasSize(2);
        assertThat(geoPolygon.getCoordinates().get(0))
                .flatExtracting("x", "y")
                .containsExactly(0., 0., 3., 6., 6., 1., 0., 0.);
        assertThat(geoPolygon.getCoordinates().get(1))
                .flatExtracting("x", "y")
                .containsExactly(2., 2., 3., 3., 4., 2., 2., 2.);
    }

    @Test
    void defineGeoMultiPoint() {
        //[[[0,0], [3,6], [6,1], [0,0]]]
        List<Point> points = Arrays.asList(new Point(0, 0), new Point(3, 6), new Point(6, 1));
        GeoMultiPoint geoMultiPoint = new GeoMultiPoint(points);

        assertThat(geoMultiPoint.getType()).isEqualTo("MultiPoint");
        assertThat(geoMultiPoint.getCoordinates()).hasSize(3);
        assertThat(geoMultiPoint.getCoordinates()).containsAll(points);
    }

    @Test
    void deserializeGeoMultiPoint() {
        String multiPointJson = "{\"type\":\"MultiPoint\",\"coordinates\":[[0,0],[3,6],[6,1]]}";
        GeoMultiPoint geoMultiPoint = (GeoMultiPoint) BeanUtil.parse(multiPointJson, GeoShape.class);

        assertThat(geoMultiPoint.getType()).isEqualTo("MultiPoint");
        assertThat(geoMultiPoint.getCoordinates())
                .hasSize(3)
                .flatExtracting("x", "y")
                .containsExactly(0., 0., 3., 6., 6., 1.);
    }

    @Test
    void deserializeGeoMultiLine() {
        String multiLineJson = "{type:\"MultiLine\",coordinates:[[[-73.96943,40.78519],[-73.96082,40.78095]],[[-73.96415,40.79229],[-73.95544,40.78854]],[[-73.97162,40.78205],[-73.96374,40.77715]]]}";
        GeoMultiLine geoMultiLine = (GeoMultiLine) BeanUtil.parse(multiLineJson, GeoShape.class);

        assertThat(geoMultiLine.getType()).isEqualTo("MultiLine");
        assertThat(geoMultiLine.getCoordinates()).hasSize(3);
        assertThat(geoMultiLine.getCoordinates().get(0))
                .flatExtracting("x", "y")
                .containsExactly(-73.96943, 40.78519, -73.96082, 40.78095);
    }

    @Test
    void deserializeMultiPolygon() {
        String multiLineJson = "{type:\"MultiPolygon\",coordinates:[" +
                "[[[-73.958,40.8003],[-73.9498,40.7968],[-73.9737,40.7648],[-73.9814,40.7681],[-73.958,40.8003]]," +
                "[[-73.9737,40.7648],[-73.9814,40.7681],[-73.958,40.8003]]]," +
                "[[[-73.958,40.8003],[-73.9498,40.7968],[-73.9737,40.7648],[-73.958,40.8003]]]" +
                "]}";
        GeoMultiPolygon multiPolygon = (GeoMultiPolygon) BeanUtil.parse(multiLineJson, GeoShape.class);

        assertThat(multiPolygon.getType()).isEqualTo("MultiPolygon");
        assertThat(multiPolygon.getCoordinates()).hasSize(2);
        assertThat(multiPolygon.getCoordinates().get(0)).hasSize(2);
        assertThat(multiPolygon.getCoordinates().get(0).get(0))
                .flatExtracting("x", "y")
                .containsExactly(-73.958, 40.8003, -73.9498, 40.7968, -73.9737, 40.7648, -73.9814, 40.7681, -73.958, 40.8003);
        assertThat(multiPolygon.getCoordinates().get(0).get(1))
                .flatExtracting("x", "y")
                .containsExactly(-73.9737, 40.7648, -73.9814, 40.7681, -73.958, 40.8003);
    }

    @Test
    void deserializeGeoCollection() throws IOException {
        GeoCollection geoCollection = (GeoCollection) BeanUtil.loadJsonData("GeometryCollection.json", new TypeReference<GeoShape>() {});

        assertThat(geoCollection.getType()).isEqualTo("GeometryCollection");
        assertThat(geoCollection.getCoordinates())
                .hasSize(2)
                .extracting("type")
                .containsExactly("MultiPoint", "MultiLine");
    }

}