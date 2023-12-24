package com.example.essentialcloud.bff;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    @Value("${spring.security.oauth2.client.provider.auth0.issuer-uri}")
    private String issuer;
    private final BffService bffService;

    public SecurityConfiguration(BffService bffService) {
        this.bffService = bffService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers( "/", "/dist/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .oauth2Login(withDefaults())
                .logout(logout->logout.logoutRequestMatcher(new AntPathRequestMatcher("/logout")).addLogoutHandler(oidcLogoutHandler()))
                .addFilterBefore(new AddAuthorityFilter(bffService), AuthorizationFilter.class);
        return http.build();
    }
    private LogoutHandler oidcLogoutHandler() {
        return (request, response, authentication) -> {
            try {
                response.sendRedirect( issuer + "/protocol/openid-connect/logout?client_id=EssentialCloud&post_logout_redirect_uri=http://localhost:8080/");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
