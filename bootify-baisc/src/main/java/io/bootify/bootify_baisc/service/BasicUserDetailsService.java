package io.bootify.bootify_baisc.service;

import io.bootify.bootify_baisc.domain.User;
import io.bootify.bootify_baisc.model.BasicUserDetails;
import io.bootify.bootify_baisc.repos.UserRepository;
import io.bootify.bootify_baisc.util.UserRoles;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class BasicUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public BasicUserDetailsService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public BasicUserDetails loadUserByUsername(final String username) {
        final User user = userRepository.findByNameIgnoreCase(username);
        if (user == null) {
            log.warn("user not found: {}", username);
            throw new UsernameNotFoundException("User " + username + " not found");
        }
        final String role = UserRoles.USER;
        final List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
        return new BasicUserDetails(user.getId(), username, user.getHash(), authorities);
    }

}
