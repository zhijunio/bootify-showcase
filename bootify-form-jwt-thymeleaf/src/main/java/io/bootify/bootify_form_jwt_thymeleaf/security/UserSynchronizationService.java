package io.bootify.bootify_form_jwt_thymeleaf.security;

import io.bootify.bootify_form_jwt_thymeleaf.user.User;
import io.bootify.bootify_form_jwt_thymeleaf.user.UserRepository;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.stereotype.Service;


/**
 * Synchronize users with the database after successful login.
 */
@Service
public class UserSynchronizationService {

    private static final Logger log = LoggerFactory.getLogger(UserSynchronizationService.class);

    private final UserRepository userRepository;

    public UserSynchronizationService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private void syncWithDatabase(final Map<String, Object> claims, final String loginType) {
        final String subject = claims.get("id").toString();
        User user = userRepository.findByOpenid(subject);
        if (user == null) {
            log.info("adding new user after successful login: {}", subject);
            user = new User();
            user.setOpenid(subject);
            user.setLoginType(loginType);
        } else {
            log.info("updating existing user after successful login: {}", subject);
        }
        userRepository.save(user);
    }

    @EventListener(AuthenticationSuccessEvent.class)
    public void onAuthenticationSuccessEvent(final AuthenticationSuccessEvent event) {
        if (event.getSource() instanceof OAuth2LoginAuthenticationToken oauthToken && 
        "github".equals(oauthToken.getClientRegistration().getRegistrationId())) {
            syncWithDatabase(oauthToken.getPrincipal().getAttributes(), oauthToken.getClientRegistration().getRegistrationId());
        }
    }

}
