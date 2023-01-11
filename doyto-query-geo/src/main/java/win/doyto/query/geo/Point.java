/*
 * Copyright © 2019-2023 Forb Yuan
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

import lombok.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Point
 *
 * @author f0rb on 2021-11-30
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Point {
    private double x;
    private double y;

    public List<Double> toList() {
        return Collections.unmodifiableList(Arrays.asList(this.x, this.y));
    }
}
