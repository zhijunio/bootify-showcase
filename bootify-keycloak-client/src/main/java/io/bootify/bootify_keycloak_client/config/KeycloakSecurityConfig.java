package io.bootify.bootify_keycloak_client.config;

import static org.springframework.security.config.Customizer.withDefaults;

import io.bootify.bootify_keycloak_client.util.UserRoles;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class KeycloakSecurityConfig {

    /**
     * Read claims from attribute realm_access.roles as SimpleGrantedAuthority.
     */
    private List<SimpleGrantedAuthority> mapAuthorities(final Map<String, Object> attributes) {
        @SuppressWarnings("unchecked") final Map<String, Object> realmAccess =
                ((Map<String, Object>)attributes.getOrDefault("realm_access", Collections.emptyMap()));
        @SuppressWarnings("unchecked") final Collection<String> roles =
                ((Collection<String>)realmAccess.getOrDefault("roles", List.of()));
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role))
                .toList();
    }

    /**
     * Custom mapper to use OIDC claims as Spring Security authorities.
     */
    @Bean
    public GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return authorities -> {
            final Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
            authorities.forEach(authority -> {
                if (authority instanceof OidcUserAuthority oidcAuth) {
                    mappedAuthorities.addAll(mapAuthorities(oidcAuth.getIdToken().getClaims()));
                } else if (authority instanceof OAuth2UserAuthority oauth2Auth) {
                    mappedAuthorities.addAll(mapAuthorities(oauth2Auth.getAttributes()));
                }
            });
            return mappedAuthorities;
        };
    }

    /**
     * Define target URL after user logout.
     */
    private OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler(
            final ClientRegistrationRepository clientRegistrationRepository) {
        final OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        // no support for camelCase
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}/?logoutsuccess=true");
        return oidcLogoutSuccessHandler;
    }

    @Bean
    public SecurityFilterChain keycloakFilterChain(final HttpSecurity http,
            final ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        return http.cors(withDefaults())
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/actuator/**"))
                .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers("/api/users/**").hasAuthority(UserRoles.USER)
                    .anyRequest().permitAll())
                .oauth2Login(withDefaults())
                .logout(logout -> logout
                    .logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository))
                    .deleteCookies("SESSION"))
                .build();
    }

}
