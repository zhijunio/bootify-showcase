package io.bootify.bootify_jwt.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UserDTO {

    private Long id;

    @NotNull
    @Size(max = 255)
    @UserNameUnique
    private String name;

    @Size(max = 255)
    private String hash;

    @Size(max = 255)
    private String openid;

    @Size(max = 255)
    private String loginType;

}
