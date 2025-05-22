package io.bootify.bootify_jwt_multi.base.model;

import io.bootify.bootify_jwt_multi.base.user.model.UserNameUnique;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public class RegistrationRequest {

    @NotNull
    @Size(max = 255)
    @UserNameUnique(message = "{registration.register.taken}")
    private String name;

    @NotNull
    @Size(max = 72)
    private String password;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

}
