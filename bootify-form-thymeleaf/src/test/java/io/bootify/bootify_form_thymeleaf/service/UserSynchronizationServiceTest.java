package io.bootify.bootify_form_thymeleaf.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.bootify.bootify_form_thymeleaf.config.BaseIT;
import io.bootify.bootify_form_thymeleaf.domain.User;
import org.junit.jupiter.api.Test;


public class UserSynchronizationServiceTest extends BaseIT {

    @Test
    public void userCreatedAfterLogin() {
        formSocialSession(true);
        final User user = userRepository.findByOpenid("3599307");
        assertNotNull(user);
        assertEquals("github", user.getLoginType());
    }

}
