package io.bootify.bootify_jwt.service;

import io.bootify.bootify_jwt.domain.User;
import io.bootify.bootify_jwt.model.RegistrationRequest;
import io.bootify.bootify_jwt.repos.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(final UserRepository userRepository,
            final PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(final RegistrationRequest registrationRequest) {
        log.info("registering new user: {}", registrationRequest.getName());

        final User user = new User();
        user.setLoginType("direct");
        user.setName(registrationRequest.getName());
        user.setHash(passwordEncoder.encode(registrationRequest.getPassword()));
        userRepository.save(user);
    }

}
