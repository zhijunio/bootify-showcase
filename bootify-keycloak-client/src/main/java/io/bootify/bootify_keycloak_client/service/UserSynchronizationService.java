package io.bootify.bootify_keycloak_client.service;

import io.bootify.bootify_keycloak_client.domain.User;
import io.bootify.bootify_keycloak_client.repos.UserRepository;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.stereotype.Service;


/**
 * Synchronize Keycloak users with the database after successful login.
 */
@Service
public class UserSynchronizationService {

    private static final Logger log = LoggerFactory.getLogger(UserSynchronizationService.class);

    private final UserRepository userRepository;

    public UserSynchronizationService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private void syncWithDatabase(final Map<String, Object> claims) {
        final String subject = claims.get("sub").toString();
        User user = userRepository.findByOpenid(subject);
        if (user == null) {
            log.info("adding new user after successful login: {}", subject);
            user = new User();
            user.setOpenid(subject);
            // TODO provide data for new users
            user.setName("Stet clita kasd.");
        } else {
            log.info("updating existing user after successful login: {}", subject);
        }
        user.setEmail(((String)claims.get("email")));
        user.setFirstNname(((String)claims.get("given_name")));
        user.setLastNamme(((String)claims.get("family_name")));
        userRepository.save(user);
    }

    @EventListener(AuthenticationSuccessEvent.class)
    public void onAuthenticationSuccessEvent(final AuthenticationSuccessEvent event) {
        if (event.getSource() instanceof OAuth2LoginAuthenticationToken oauthToken && 
        "test-client".equals(oauthToken.getClientRegistration().getClientId())) {
            syncWithDatabase(oauthToken.getPrincipal().getAttributes());
        }
    }

}
