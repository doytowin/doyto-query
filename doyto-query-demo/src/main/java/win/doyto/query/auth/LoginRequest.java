package win.doyto.query.auth;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * LoginRequest
 *
 * @author f0rb
 * @date 2019-05-16
 */
@Getter
@Setter
public class LoginRequest {
    @NotNull
    private String account;

    @NotNull
    private String password;
}
