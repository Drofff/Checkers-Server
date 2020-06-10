package com.drofff.checkers.server.service;

import com.drofff.checkers.server.document.Session;
import com.drofff.checkers.server.document.User;
import com.drofff.checkers.server.message.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SessionService {

    Mono<Session> initSessionWithUserHavingNickname(String nickname);

    Flux<Message> joinSessionWithId(String id);

    Mono<Void> updateSession(Session session);

    Mono<Session> getCurrentSession();

    Mono<Session> getSessionOfUser(User user);

}