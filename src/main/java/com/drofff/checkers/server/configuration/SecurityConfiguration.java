package com.drofff.checkers.server.configuration;

import com.drofff.checkers.server.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableRSocketSecurity
public class SecurityConfiguration {

    @Bean
    public PayloadSocketAcceptorInterceptor securityWebFilterChain(RSocketSecurity rSocketSecurity) {
        return rSocketSecurity.authorizePayload(spec -> spec.anyExchange().permitAll())
                .simpleAuthentication(withDefaults())
                .build();
    }

    @Bean
    public ReactiveUserDetailsService reactiveUserDetailsService(UserService userService) {
        return email -> userService.getUserByEmail(email)
                .map(user -> user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}