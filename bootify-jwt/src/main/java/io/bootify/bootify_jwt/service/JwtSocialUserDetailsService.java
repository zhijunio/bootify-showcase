package io.bootify.bootify_jwt.service;

import io.bootify.bootify_jwt.domain.User;
import io.bootify.bootify_jwt.model.JwtUserDetails;
import io.bootify.bootify_jwt.repos.UserRepository;
import io.bootify.bootify_jwt.util.UserRoles;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class JwtSocialUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public JwtSocialUserDetailsService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public JwtUserDetails loadUserByUsername(final String username) {
        final User user = userRepository.findByOpenid(username);
        if (user == null) {
            log.warn("user not found: {}", username);
            throw new UsernameNotFoundException("User " + username + " not found");
        }
        final String role = UserRoles.USER;
        final List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
        return new JwtUserDetails(user.getId(), username, "", authorities);
    }

}
