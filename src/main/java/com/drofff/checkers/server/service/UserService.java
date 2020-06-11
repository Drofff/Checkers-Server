package com.drofff.checkers.server.service;

import com.drofff.checkers.server.document.User;
import reactor.core.publisher.Mono;

public interface UserService {

    Mono<User> getUserById(String id);

    Mono<User> getUserByNickname(String nickname);

    Mono<User> getUserByEmail(String email);

    Mono<Void> updateUser(User user);

}