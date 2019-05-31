package win.doyto.query.demo.module.auth;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * LoginRequest
 *
 * @author f0rb
 */
@Getter
@Setter
public class LoginRequest {
    @NotNull
    private String account;

    @NotNull
    private String password;
}
