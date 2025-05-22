package io.bootify.bootify_jwt_multi.base.service;

import io.bootify.bootify_jwt_multi.base.model.JwtUserDetails;
import io.bootify.bootify_jwt_multi.base.user.domain.User;
import io.bootify.bootify_jwt_multi.base.user.repos.UserRepository;
import io.bootify.bootify_jwt_multi.base.util.UserRoles;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class JwtUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(JwtUserDetailsService.class);

    private final UserRepository userRepository;

    public JwtUserDetailsService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public JwtUserDetails loadUserByUsername(final String username) {
        final User user = userRepository.findByNameIgnoreCase(username);
        if (user == null) {
            log.warn("user not found: {}", username);
            throw new UsernameNotFoundException("User " + username + " not found");
        }
        final String role = UserRoles.USER;
        final List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
        return new JwtUserDetails(user.getId(), username, user.getHash(), authorities);
    }

}
