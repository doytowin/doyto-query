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

package win.doyto.query.web.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

/**
 * PresetErrorCode
 *
 * @author f0rb
 */
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum PresetErrorCode implements ErrorCode {
    SUCCESS,

    INTERNAL_ERROR,
    REQUEST_BODY_ERROR,
    ARGUMENT_TYPE_MISMATCH,
    ARGUMENT_FORMAT_ERROR,
    ARGUMENT_VALIDATION_FAILED,
    HTTP_METHOD_NOT_SUPPORTED,
    DUPLICATE_KEY_EXCEPTION,
    FILE_UPLOAD_OVER_MAX_SIZE,
    ENTITY_NOT_FOUND,

    ;

    private final Integer code;

    PresetErrorCode() {
        this.code = Index.count++;
    }

    public String getMessage() {
        return super.name();
    }

    private static class Index {
        private static int count = 0;
    }
}
