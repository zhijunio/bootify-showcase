package io.bootify.bootify_jwt.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AuthenticationRequest {

    @NotNull
    @Size(max = 255)
    private String name;

    @NotNull
    @Size(max = 72)
    private String password;

}
