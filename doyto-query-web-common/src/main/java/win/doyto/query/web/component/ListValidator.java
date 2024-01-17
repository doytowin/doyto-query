/*
 * Copyright © 2019-2024 Forb Yuan
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

import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.SmartValidator;
import win.doyto.query.validation.CreateGroup;
import win.doyto.query.web.response.ErrorCode;
import win.doyto.query.web.response.ErrorResponse;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static win.doyto.query.web.response.PresetErrorCode.ARGUMENT_VALIDATION_FAILED;

/**
 * ListValidator
 *
 * @author f0rb on 2020-04-25
 */
public class ListValidator {

    @Resource
    private SmartValidator smartValidator;

    public void validateList(List<?> list) {
        if (smartValidator == null) {
            return;
        }
        int errorCount = 0;
        List<BindingResult> bindingResults = new ArrayList<>();
        for (Object r : list) {
            BindingResult bindingResult = new BeanPropertyBindingResult(r, r.getClass().getSimpleName());
            smartValidator.validate(r, bindingResult, CreateGroup.class);
            bindingResults.add(bindingResult);
            errorCount += bindingResult.getErrorCount();
        }
        ErrorCode.assertFalse(errorCount > 0, new ErrorResponse(ARGUMENT_VALIDATION_FAILED, bindingResults));
    }
}
