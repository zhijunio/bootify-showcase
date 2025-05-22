package io.bootify.bootify_jwt_multi.base.model;

import jakarta.validation.constraints.NotNull;


public class AuthenticationSocialRequest {

    @NotNull
    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

}
