package win.doyto.query.web.component;

import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;

/**
 * MockValidator
 *
 * @author f0rb on 2020-04-25
 */
class MockValidator implements SmartValidator {
    @Override
    public void validate(Object target, Errors errors, Object... validationHints) {
        validate(target, errors);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return false;
    }

    @Override
    public void validate(Object target, Errors errors) {
        // mock
    }
}
