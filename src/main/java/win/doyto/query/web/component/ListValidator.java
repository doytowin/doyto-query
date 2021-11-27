package win.doyto.query.web.component;

import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.SmartValidator;
import win.doyto.query.validation.CreateGroup;
import win.doyto.query.web.response.ErrorCode;
import win.doyto.query.web.response.ErrorResponse;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;

import static win.doyto.query.web.response.PresetErrorCode.ARGUMENT_VALIDATION_FAILED;

/**
 * ListValidator
 *
 * @author f0rb on 2020-04-25
 */
@Component
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
