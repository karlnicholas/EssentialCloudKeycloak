package com.example.essentialcloud.bff;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfiguration {
    @Value("${spring.security.oauth2.client.provider.auth0.issuer-uri}")
    private String issuer;
//    @Value("${spring.security.oauth2.client.registration.auth0-login.client-id}")
//    private String clientId;
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
                .addFilterBefore(new AddAuthorityFilter(), AuthorizationFilter.class);
        return http.build();
    }
    class AddAuthorityFilter implements Filter {
        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (!(authentication instanceof AnonymousAuthenticationToken)) {
                try {
                    Boolean transferAdmin = bffService.getUserInfo(((DefaultOidcUser) authentication.getPrincipal()).getClaims().get("sub").toString());
                    if (transferAdmin) {
                        Collection<SimpleGrantedAuthority> oldAuthorities = (Collection<SimpleGrantedAuthority>) SecurityContextHolder.getContext().getAuthentication().getAuthorities();
                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_TRANSFERADMIN");
                        List<SimpleGrantedAuthority> updatedAuthorities = new ArrayList<>();
                        updatedAuthorities.add(authority);
                        updatedAuthorities.addAll(oldAuthorities);

                        SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken(
                                        SecurityContextHolder.getContext().getAuthentication().getPrincipal(),
                                        SecurityContextHolder.getContext().getAuthentication().getCredentials(),
                                        updatedAuthorities)
                        );
                    }
                } catch (Exception ex) {
                    log.error("Error finding user info" + ((DefaultOidcUser)authentication.getPrincipal()).getClaims().get("sub").toString(), ex);
                }
            }
            filterChain.doFilter(request, response);
        }

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
