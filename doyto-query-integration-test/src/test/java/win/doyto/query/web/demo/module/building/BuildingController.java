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

package win.doyto.query.web.demo.module.building;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import win.doyto.query.geo.GeoShape;
import win.doyto.query.web.component.GeoShapeEditor;
import win.doyto.query.web.controller.AbstractEIQController;

/**
 * InventoryController
 *
 * @author f0rb on 2021-12-06
 */
@RestController
@RequestMapping("building")
public class BuildingController extends AbstractEIQController<BuildingEntity, String, BuildingQuery> {

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        dataBinder.registerCustomEditor(GeoShape.class, new GeoShapeEditor());
    }
}
