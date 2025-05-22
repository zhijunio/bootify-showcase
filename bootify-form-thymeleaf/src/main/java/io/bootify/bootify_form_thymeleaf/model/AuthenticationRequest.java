package io.bootify.bootify_form_thymeleaf.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public class AuthenticationRequest {

    @NotNull
    @Size(max = 255)
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
