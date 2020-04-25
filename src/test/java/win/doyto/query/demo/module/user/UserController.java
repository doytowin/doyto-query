package win.doyto.query.demo.module.user;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import win.doyto.query.web.controller.AbstractRestController;
import win.doyto.query.web.response.ErrorCode;
import win.doyto.query.web.response.JsonBody;
import win.doyto.query.web.response.JsonResponse;

import javax.validation.Valid;
import javax.validation.constraints.Size;

/**
 * UserController
 *
 * @author f0rb on 2020-04-01
 */
@RestController
@RequestMapping("user")
@JsonBody
@Validated
public class UserController extends AbstractRestController<UserEntity, Long, UserQuery, UserRequest, UserResponse> {
    @GetMapping("/username")
    public UserResponse getByUsername(@Size(min = 4, max = 20) String username) {
        UserEntity userEntity = service.get(UserQuery.builder().username(username).build());
        return buildResponse(userEntity);
    }

    @GetMapping("/email")
    public JsonResponse<UserResponse> getByEmail(@Valid UserEmailRequest userEmailRequest) {
        UserEntity userEntity = service.get(UserQuery.builder().email(userEmailRequest.getEmail()).build());
        return ErrorCode.build(buildResponse(userEntity));
    }

}
