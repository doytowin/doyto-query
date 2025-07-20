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

package win.doyto.query.web.component;

import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import win.doyto.query.web.response.ErrorCode;
import win.doyto.query.web.response.ErrorResponse;

import java.util.Locale;

/**
 * ErrorCodeI18nService
 *
 * @author f0rb on 2021-12-15
 */
@AllArgsConstructor
public class ErrorCodeI18nService {

    private MessageSource messageSource;

    public ErrorCode buildErrorCode(ErrorCode errorCode, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(errorCode.getMessage(), args, errorCode.getMessage(), locale);
        ErrorCode localeErrorCode = ErrorCode.build(errorCode.getCode(), message);
        if (errorCode instanceof ErrorResponse) {
            ((ErrorResponse) errorCode).setErrorCode(localeErrorCode);
            localeErrorCode = errorCode;
        }
        return localeErrorCode;
    }

}
