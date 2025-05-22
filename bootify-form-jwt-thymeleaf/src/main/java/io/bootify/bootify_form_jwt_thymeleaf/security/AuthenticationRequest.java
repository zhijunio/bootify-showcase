package io.bootify.bootify_form_jwt_thymeleaf.security;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public class AuthenticationRequest {

    @NotNull
    @Size(max = 255)
    private String name;

    @NotNull
    @Size(max = 72)
    private String password;

    @NotNull
    private Boolean rememberMe;

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

    public Boolean getRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(final Boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

}
