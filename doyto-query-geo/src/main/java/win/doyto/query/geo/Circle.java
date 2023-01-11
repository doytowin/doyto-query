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

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Circle
 *
 * @author f0rb on 2021-12-01
 */
@Getter
@Setter
public class Circle {
    @NonNull
    private Point center;
    private double radius;

    public double getX() {
        return center.getX();
    }

    public double getY() {
        return center.getY();
    }
}
