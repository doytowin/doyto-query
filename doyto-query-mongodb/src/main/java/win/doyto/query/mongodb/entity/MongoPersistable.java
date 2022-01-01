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

package win.doyto.query.mongodb.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;
import javax.persistence.GeneratedValue;

/**
 * AbstractMongoPersistable
 *
 * @author f0rb on 2021-11-23
 */
@Getter
@Setter
public abstract class MongoPersistable<I extends Serializable> implements Persistable<I>, ObjectIdAware {

    @GeneratedValue
    private I id;

    protected MongoPersistable() {
        ObjectIdMapper.initIdMapper(this.getClass());
    }

    @JsonProperty("_id")
    @JsonSerialize(using = ObjectIdSerializer.class)
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    private ObjectId objectId;

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
        this.setId(ObjectIdMapper.convert(this.getClass(), objectId));
    }
}
