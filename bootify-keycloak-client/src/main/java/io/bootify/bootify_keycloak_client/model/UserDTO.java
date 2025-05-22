package io.bootify.bootify_keycloak_client.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


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

    @Size(max = 255)
    private String email;

    @Size(max = 255)
    private String firstNname;

    @Size(max = 255)
    private String lastNamme;

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

    public String getHash() {
        return hash;
    }

    public void setHash(final String hash) {
        this.hash = hash;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getFirstNname() {
        return firstNname;
    }

    public void setFirstNname(final String firstNname) {
        this.firstNname = firstNname;
    }

    public String getLastNamme() {
        return lastNamme;
    }

    public void setLastNamme(final String lastNamme) {
        this.lastNamme = lastNamme;
    }

}
