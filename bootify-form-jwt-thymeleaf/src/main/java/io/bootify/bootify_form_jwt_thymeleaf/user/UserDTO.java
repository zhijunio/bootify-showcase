package io.bootify.bootify_form_jwt_thymeleaf.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public class UserDTO {

    private Long id;

    @NotNull
    @Size(max = 255)
    @UserNameUnique
    private String name;

    @Size(max = 255)
    private String openid;

    @Size(max = 255)
    private String loginType;

    @Size(max = 255)
    private String hash;

    private Long role;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(final String openid) {
        this.openid = openid;
    }

    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(final String loginType) {
        this.loginType = loginType;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(final String hash) {
        this.hash = hash;
    }

    public Long getRole() {
        return role;
    }

    public void setRole(final Long role) {
        this.role = role;
    }

}
