package io.bootify.bootify_form_jwt_thymeleaf.security;

import io.bootify.bootify_form_jwt_thymeleaf.user.User;
import io.bootify.bootify_form_jwt_thymeleaf.user.UserRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class FormUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(FormUserDetailsService.class);

    private final UserRepository userRepository;

    public FormUserDetailsService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public FormUserDetails loadUserByUsername(final String username) {
        final User user = userRepository.findByNameIgnoreCase(username);
        if (user == null) {
            log.warn("user not found: {}", username);
            throw new UsernameNotFoundException("User " + username + " not found");
        }
        final String role = UserRoles.USER;
        final List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
        return new FormUserDetails(user.getId(), username, user.getHash(), authorities);
    }

}
