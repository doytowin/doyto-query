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

package win.doyto.query.geo;

import org.junit.jupiter.api.Test;
import win.doyto.query.util.BeanUtil;

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
}