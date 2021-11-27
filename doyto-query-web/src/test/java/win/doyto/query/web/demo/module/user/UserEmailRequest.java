package win.doyto.query.web.demo.module.user;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * UserIdRequest
 *
 * @author f0rb on 2020-04-25
 */
@Data
class UserEmailRequest {
    @NotNull
    private String email;
}
