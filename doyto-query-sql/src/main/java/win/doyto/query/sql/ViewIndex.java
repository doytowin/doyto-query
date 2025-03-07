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

package win.doyto.query.sql;

import lombok.Getter;
import win.doyto.query.annotation.View;

import java.util.List;
import java.util.Optional;

/**
 * ViewIndex
 *
 * @author f0rb on 2023/6/11
 * @since 1.0.2
 */
class ViewIndex {
    ViewIndex(View view) {
        this.entity = view.value();
        this.alias = view.alias();
    }

    ViewIndex(Class<?> entity) {
        this.entity = entity;
        this.alias = "";
    }

    @Getter
    private Class<?> entity;
    private String alias;
    private int vote = 0; // entity is available for foreign key connection when vote >= 0

    static ViewIndex searchEntity(List<ViewIndex> viewList, Class<?> entity) {
        List<ViewIndex> list = viewList.stream().filter(vi -> vi.entity == entity).toList();
        if (list.size() > 1) {
            Optional<ViewIndex> first = list.stream().filter(ViewIndex::valid).findFirst();
            first.ifPresent(ViewIndex::voteDown);
            return first.orElse(null);
        }
        return list.size() == 1 ? list.get(0) : null;
    }

    boolean valid() {
        return vote >= 0;
    }

    void voteUp() {
        vote++;
    }

    void voteDown() {
        vote--;
    }

    String getAlias() {
        return alias.isEmpty() ? "" : alias + ".";
    }

}
