/*
 * Copyright © 2025 DoytoWin, Inc.
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
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import win.doyto.query.validation.CreateGroup;
import win.doyto.query.web.response.ErrorCode;
import win.doyto.query.web.response.ErrorResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static win.doyto.query.web.response.PresetErrorCode.ARGUMENT_VALIDATION_FAILED;

@ControllerAdvice
public class CollectionValidator implements SmartValidator {
    protected LocalValidatorFactoryBean validator;

    public CollectionValidator(LocalValidatorFactoryBean validatorFactory) {
        this.validator = validatorFactory;
    }

    /**
     * Adds the {@link CollectionValidator} to the supplied
     * {@link WebDataBinder}
     *
     * @param binder web data binder.
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        Class<?> type = getTargetType(binder);
        if (type != null && this.supports(type)) {
            binder.addValidators(this);
        }
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Collection.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        this.validate(target, errors, CreateGroup.class);
    }

    @Override
    public void validate(Object target, Errors errors, Object... validationHints) {
        Collection<?> collection = (Collection<?>) target;
        if (collection.size() == 1) {
            validator.validate(collection.toArray()[0], errors, validationHints);
        } else {
            int errorCount = 0;
            List<BindingResult> bindingResults = new ArrayList<>();
            for (Object entry : collection) {
                BindingResult bindingResult = new BeanPropertyBindingResult(entry, entry.getClass().getSimpleName());
                validator.validate(entry, bindingResult, validationHints);
                bindingResults.add(bindingResult);
                errorCount += bindingResult.getErrorCount();
            }
            ErrorCode.assertFalse(errorCount > 0, new ErrorResponse(ARGUMENT_VALIDATION_FAILED, bindingResults));
        }
    }

    private static Class<?> getTargetType(WebDataBinder binder) {
        Class<?> type = null;
        if (binder.getTarget() != null) {
            type = binder.getTarget().getClass();
        } else if (binder.getTargetType() != null) {
            type = binder.getTargetType().resolve();
        }

        return type;
    }
}