package io.bootify.bootify_jwt_multi.base.config;

import static org.springframework.security.config.Customizer.withDefaults;

import io.bootify.bootify_jwt_multi.base.service.JwtSocialUserDetailsService;
import io.bootify.bootify_jwt_multi.base.service.JwtTokenService;
import io.bootify.bootify_jwt_multi.base.service.JwtUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableMethodSecurity(prePostEnabled = true)
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
    public SecurityFilterChain jwtFilterChain(final HttpSecurity http,
            final JwtUserDetailsService jwtUserDetailsService,
            final JwtSocialUserDetailsService jwtSocialUserDetailsService,
            final JwtTokenService jwtTokenService) throws Exception {
        return http.cors(withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtRequestFilter(jwtUserDetailsService, jwtSocialUserDetailsService, jwtTokenService), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}
