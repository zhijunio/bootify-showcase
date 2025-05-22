package io.bootify.bootify_form_jwt_thymeleaf.security;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;


/**
 * Extension of Spring Security User class to store additional data.
 */
public class FormUserDetails extends User {

    private final Long id;

    public FormUserDetails(final Long id, final String username, final String hash,
            final Collection<? extends GrantedAuthority> authorities) {
        super(username, hash, authorities);
        this.id = id;
    }

    public Long getId() {
        return id;
    }

}
