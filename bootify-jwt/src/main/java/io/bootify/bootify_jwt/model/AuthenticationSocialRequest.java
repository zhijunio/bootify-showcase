package io.bootify.bootify_jwt.model;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AuthenticationSocialRequest {

    @NotNull
    private String code;

}
