package io.bootify.bootify_jwt.rest;

import io.bootify.bootify_jwt.domain.User;
import io.bootify.bootify_jwt.model.AuthenticationRequest;
import io.bootify.bootify_jwt.model.AuthenticationResponse;
import io.bootify.bootify_jwt.model.AuthenticationSocialRequest;
import io.bootify.bootify_jwt.model.JwtUserDetails;
import io.bootify.bootify_jwt.repos.UserRepository;
import io.bootify.bootify_jwt.service.JwtSocialUserDetailsService;
import io.bootify.bootify_jwt.service.JwtTokenService;
import io.bootify.bootify_jwt.service.JwtUserDetailsService;
import jakarta.validation.Valid;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;


@RestController
@Slf4j
public class AuthenticationResource {

    private final AuthenticationProvider authenticationProvider;
    private final JwtUserDetailsService jwtUserDetailsService;
    private final JwtSocialUserDetailsService jwtSocialUserDetailsService;
    private final JwtTokenService jwtTokenService;
    private final Environment environment;
    private final UserRepository userRepository;
    private final String baseHost;
    private final RestClient githubClient = RestClient.builder()
            .baseUrl("https://github.com/")
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    private final RestClient githubApiClient = RestClient.builder()
            .baseUrl("https://api.github.com/")
            .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
            .build();

    public AuthenticationResource(final AuthenticationProvider authenticationProvider,
            final JwtUserDetailsService jwtUserDetailsService,
            final JwtSocialUserDetailsService jwtSocialUserDetailsService,
            final JwtTokenService jwtTokenService, final Environment environment,
            final UserRepository userRepository, @Value("${app.baseHost}") final String baseHost) {
        this.authenticationProvider = authenticationProvider;
        this.jwtUserDetailsService = jwtUserDetailsService;
        this.jwtSocialUserDetailsService = jwtSocialUserDetailsService;
        this.jwtTokenService = jwtTokenService;
        this.environment = environment;
        this.userRepository = userRepository;
        this.baseHost = baseHost;
    }

    @PostMapping("/authenticate")
    public AuthenticationResponse authenticate(
            @RequestBody @Valid final AuthenticationRequest authenticationRequest) {
        try {
            authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(
                    authenticationRequest.getName(), authenticationRequest.getPassword()));
        } catch (final BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        final JwtUserDetails userDetails = jwtUserDetailsService.loadUserByUsername(authenticationRequest.getName());
        final AuthenticationResponse authenticationResponse = new AuthenticationResponse();
        authenticationResponse.setAccessToken(jwtTokenService.generateToken(userDetails, "direct", null));
        return authenticationResponse;
    }

    private AuthenticationResponse synchronizeUserAndGetToken(final String loginType,
            final String subject, final Instant expiresAt) {
        User user = userRepository.findByOpenid(subject);
        if (user == null) {
            log.info("adding new user after successful authentication: {}", subject);
            user = new User();
            user.setOpenid(subject);
            user.setLoginType(loginType);
        } else {
            log.info("updating existing user after successful authentication: {}", subject);
        }
        userRepository.save(user);

        final JwtUserDetails userDetails = jwtSocialUserDetailsService.loadUserByUsername(subject);
        final Duration validity = Duration.between(Instant.now(), expiresAt);
        final AuthenticationResponse authenticationResponse = new AuthenticationResponse();
        authenticationResponse.setAccessToken(jwtTokenService.generateToken(userDetails, loginType, validity));
        return authenticationResponse;
    }

    @PostMapping("/authenticateGithub")
    public AuthenticationResponse authenticateGithub(
            @RequestBody @Valid final AuthenticationSocialRequest authenticationSocialRequest) {
        log.info("exchanging github code");
        final String providerId = "github";
        final String clientId = environment.getProperty("app." + providerId + ".client-id");
        final String clientSecret = environment.getProperty("app." + providerId + ".client-secret");
        final RestClient.ResponseSpec accessTokenSpec = githubClient.post()
                .uri("login/oauth/access_token")
                .body(Map.of("client_id", clientId, "client_secret", clientSecret, "code", authenticationSocialRequest.getCode()))
                .retrieve();
        final Map<String, Object> accessTokenResponse = accessTokenSpec.body(new ParameterizedTypeReference<>() {
        });

        log.info("validating github access token");
        final RestClient.ResponseSpec userSpec = githubApiClient.post()
                .uri(uriBuilder -> uriBuilder
                    .pathSegment("applications")
                    .pathSegment(clientId)
                    .pathSegment("token")
                    .build())
                .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes()))
                .body(Map.of("access_token", accessTokenResponse.get("access_token").toString()))
                .retrieve();
        final Map<String, Object> userResponse = userSpec.body(new ParameterizedTypeReference<>() {
        });
        @SuppressWarnings("unchecked") final Map<String, Object> appDetails = ((Map<String, Object>)userResponse.get("app"));
        if (!clientId.equals(appDetails.get("client_id"))) {
            log.warn("github app id not matching");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        @SuppressWarnings("unchecked") final Map<String, Object> userDetails = ((Map<String, Object>)userResponse.get("user"));
        final String subject = userDetails.get("id").toString();
        final Instant expiresAt = Instant.now().plus(Duration.ofSeconds(((Integer)accessTokenResponse.get("expires_in"))));
        if (expiresAt.isBefore(Instant.now())) {
            log.warn("github token has expired");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return synchronizeUserAndGetToken(providerId, subject, expiresAt);
    }

}
