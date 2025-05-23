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

package win.doyto.query.web.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Set;

/**
 * SortFieldsProperties
 *
 * @author f0rb on 2023/12/2
 * @since 1.0.3
 */
@Getter
@Setter
@ConfigurationProperties(
        prefix = "doyto.query.config"
)
public class SortFieldsProperties {
    private String sortPrefix = "sort.";
    private HashMap<Class<?>, Set<String>> sortFieldsMap = new HashMap<>();
}
