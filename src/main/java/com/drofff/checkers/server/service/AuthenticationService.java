package com.drofff.checkers.server.service;

import com.drofff.checkers.server.document.User;
import com.drofff.checkers.server.type.UserActivation;
import org.springframework.data.util.Pair;
import reactor.core.publisher.Mono;

public interface AuthenticationService {

    Mono<User> authenticate(Mono<Pair<String, String>> emailAndPasswordMono);

    Mono<Void> registerUser(Mono<User> userMono);

    Mono<Void> activateUser(Mono<UserActivation> userActivationMono);

}