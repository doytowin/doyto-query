package win.doyto.query.web.demo.module.building;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.entity.MongoEntity;
import win.doyto.query.mongodb.entity.MongoPersistable;

/**
 * BuildingEntity
 *
 * @author f0rb on 2021-12-06
 */
@Getter
@Setter
@MongoEntity(database = "doyto", collection = "building")
public class BuildingEntity extends MongoPersistable<String> {
    private String name;
}
