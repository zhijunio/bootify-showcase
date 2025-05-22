package io.bootify.bootify_form_thymeleaf.service;

import io.bootify.bootify_form_thymeleaf.domain.User;
import io.bootify.bootify_form_thymeleaf.model.RegistrationRequest;
import io.bootify.bootify_form_thymeleaf.repos.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class RegistrationService {

    private static final Logger log = LoggerFactory.getLogger(RegistrationService.class);

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
