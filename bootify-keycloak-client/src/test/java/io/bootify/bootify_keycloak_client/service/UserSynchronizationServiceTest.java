package io.bootify.bootify_keycloak_client.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.bootify.bootify_keycloak_client.config.BaseIT;
import io.bootify.bootify_keycloak_client.domain.User;
import org.junit.jupiter.api.Test;


public class UserSynchronizationServiceTest extends BaseIT {

    @Test
    public void userCreatedAfterLogin() {
        keycloakSession(true);
        final User user = userRepository.findByOpenid("7b529f29-bc0e-3988-b016-14dc83035fc7");
        assertNotNull(user);
        assertEquals("user@invalid.bootify.io", user.getEmail());
        assertEquals("Bob", user.getFirstNname());
        assertEquals("Lazar", user.getLastNamme());
    }

}
