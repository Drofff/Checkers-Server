package com.drofff.checkers.server.controller;

import com.drofff.checkers.server.document.Session;
import com.drofff.checkers.server.dto.SessionDto;
import com.drofff.checkers.server.message.Message;
import com.drofff.checkers.server.service.SessionService;
import com.drofff.checkers.server.service.UserService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.drofff.checkers.server.utils.MessageUtils.resumeWithErrorMessage;
import static com.drofff.checkers.server.utils.MessageUtils.toSessionIdMessage;
import static com.drofff.checkers.server.utils.SecurityUtils.getCurrentUser;

@RestController
@MessageMapping("session")
public class SessionController {

    private final SessionService sessionService;
    private final UserService userService;

    public SessionController(SessionService sessionService, UserService userService) {
        this.sessionService = sessionService;
        this.userService = userService;
    }

    @MessageMapping(".init")
    public Mono<Message> initSession(@Payload Mono<SessionDto> sessionDtoMono) {
        Mono<Session> sessionMono = sessionDtoMono.map(SessionDto::getNickname)
                .flatMap(sessionService::initSessionWithUserHavingNickname);
        return toSessionIdMessage(sessionMono);
    }

    @MessageMapping(".with.{nickname}")
    public Mono<Message> getSessionWithOpponentHavingNickname(@DestinationVariable String nickname) {
        Mono<Session> sessionMono = userService.getUserByNickname(nickname)
                .flatMap(sessionService::getSessionOfUser)
                .flatMap(session -> getCurrentUser()
                        .flatMap(user -> session.hasUser(user) ? Mono.just(session) : Mono.empty())
                );
        return toSessionIdMessage(sessionMono);
    }

    @MessageMapping(".join.{id}")
    public Flux<Message> joinSession(@DestinationVariable String id) {
        return sessionService.joinSessionWithId(id)
                .onErrorResume(resumeWithErrorMessage());
    }

}