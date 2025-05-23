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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * GeoMultiPolygon
 *
 * @author f0rb on 2022-03-15
 */
@Getter
@Setter
@AllArgsConstructor
public class GeoMultiPolygon implements GeoShape<List<List<List<Point>>>> {

    private List<List<List<Point>>> coordinates;

    @Override
    public String getType() {
        return GeoType.MULTI_POLYGON;
    }

}