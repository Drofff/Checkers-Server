package com.drofff.checkers.server.utils;

import com.drofff.checkers.server.document.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import reactor.core.publisher.Mono;

import static reactor.core.publisher.Mono.error;

public class SecurityUtils {

    private SecurityUtils() {}

    public static void setCurrentUser(User user) {
        getSecurityContext()
                .doOnNext(securityContext -> {
                    UsernamePasswordAuthenticationToken authenticationToken = user
                            .toUsernamePasswordAuthenticationToken();
                    securityContext.setAuthentication(authenticationToken);
                }).block();
    }

    public static Mono<User> getCurrentUser() {
        return getSecurityContext().map(SecurityContext::getAuthentication)
                .filter(SecurityUtils::isAuthenticated)
                .map(authentication -> (User) authentication.getPrincipal())
                .switchIfEmpty(error(new SecurityException("User is not currently authenticated")));
    }

    private static boolean isAuthenticated(Authentication authentication) {
        return authentication.getPrincipal() instanceof User;
    }

    private static Mono<SecurityContext> getSecurityContext() {
        return ReactiveSecurityContextHolder.getContext();
    }

}