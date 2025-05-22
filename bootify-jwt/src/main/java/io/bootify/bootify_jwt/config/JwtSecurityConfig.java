package io.bootify.bootify_jwt.config;

import static org.springframework.security.config.Customizer.withDefaults;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.bootify.bootify_jwt.service.JwtSocialUserDetailsService;
import io.bootify.bootify_jwt.service.JwtTokenService;
import io.bootify.bootify_jwt.service.JwtUserDetailsService;
import io.bootify.bootify_jwt.util.UserRoles;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.UnauthorizedEntryPoint;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorCodeMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorMessageMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.HttpStatusMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
public class JwtSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // creates hashes with {bcrypt} prefix
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(final PasswordEncoder passwordEncoder,
            final JwtUserDetailsService jwtUserDetailsService) {
        final DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(passwordEncoder);
        authenticationProvider.setUserDetailsService(jwtUserDetailsService);
        return authenticationProvider;
    }

    public JwtRequestFilter jwtRequestFilter(final JwtUserDetailsService jwtUserDetailsService,
            final JwtSocialUserDetailsService jwtSocialUserDetailsService,
            final JwtTokenService jwtTokenService) {
        return new JwtRequestFilter(jwtUserDetailsService, jwtSocialUserDetailsService, jwtTokenService);
    }

    @Bean
    public UnauthorizedEntryPoint unauthorizedEntryPoint(final HttpStatusMapper httpStatusMapper,
            final ErrorCodeMapper errorCodeMapper, final ErrorMessageMapper errorMessageMapper,
            final ObjectMapper objectMapper) {
        return new UnauthorizedEntryPoint(httpStatusMapper, errorCodeMapper, errorMessageMapper, objectMapper);
    }

    @Bean
    public SecurityFilterChain jwtFilterChain(final HttpSecurity http,
            final UnauthorizedEntryPoint unauthorizedEntryPoint,
            final JwtUserDetailsService jwtUserDetailsService,
            final JwtSocialUserDetailsService jwtSocialUserDetailsService,
            final JwtTokenService jwtTokenService) throws Exception {
        return http.cors(withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers("/api/users/**").hasAuthority(UserRoles.USER)
                    .anyRequest().permitAll())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedEntryPoint))
                .addFilterBefore(jwtRequestFilter(jwtUserDetailsService, jwtSocialUserDetailsService, jwtTokenService), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}
