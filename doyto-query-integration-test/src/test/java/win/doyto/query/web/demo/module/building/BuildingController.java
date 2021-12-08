package win.doyto.query.web.demo.module.building;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import win.doyto.query.web.controller.AbstractEIQController;

/**
 * InventoryController
 *
 * @author f0rb on 2021-12-06
 */
@RestController
@RequestMapping("building")
public class BuildingController extends AbstractEIQController<BuildingEntity, String, BuildingQuery> {
}
