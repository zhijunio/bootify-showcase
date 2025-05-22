package io.bootify.bootify_form_thymeleaf.config;

import static org.springframework.security.config.Customizer.withDefaults;

import io.bootify.bootify_form_thymeleaf.util.UserRoles;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;


@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class FormSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // creates hashes with {bcrypt} prefix
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            final AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public GrantedAuthoritiesMapper formUserAuthoritiesMapper() {
        return oauthAuthorities -> {
            final String role = UserRoles.USER;
            final List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
            return authorities;
        };
    }

    @Bean
    public SecurityFilterChain formFilterChain(final HttpSecurity http,
            @Qualifier("formUserAuthoritiesMapper") final GrantedAuthoritiesMapper formUserAuthoritiesMapper)
            throws Exception {
        return http.cors(withDefaults())
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/actuator/**"))
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .formLogin(form -> form
                    .loginPage("/login")
                    .usernameParameter("name")
                    .failureUrl("/login?loginError=true"))
                .oauth2Login(oauth2 -> oauth2
                    .loginPage("/login")
                    .failureUrl("/login?loginError=true")
                    .userInfoEndpoint(userInfo -> userInfo
                        .userAuthoritiesMapper(formUserAuthoritiesMapper)))
                .logout(logout -> logout
                    .logoutSuccessUrl("/?logoutSuccess=true")
                    .deleteCookies("SESSION"))
                .exceptionHandling(exception -> exception
                    .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login?loginRequired=true")))
                .build();
    }

}
