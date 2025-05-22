package io.bootify.bootify_form_jwt_thymeleaf.role;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public class RoleDTO {

    private Long id;

    @NotNull
    @Size(max = 255)
    @RoleNameUnique
    private String name;

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

}
