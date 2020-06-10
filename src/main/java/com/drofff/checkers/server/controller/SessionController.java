package com.drofff.checkers.server.controller;

import com.drofff.checkers.server.document.Session;
import com.drofff.checkers.server.dto.SessionDto;
import com.drofff.checkers.server.message.Message;
import com.drofff.checkers.server.service.SessionService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.drofff.checkers.server.utils.MessageUtils.resumeWithErrorMessage;
import static com.drofff.checkers.server.utils.MessageUtils.toSessionIdMessage;

@RestController
@MessageMapping("session")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @MessageMapping(".init")
    public Mono<Message> initSession(@Payload Mono<SessionDto> sessionDtoMono) {
        Mono<Session> sessionMono = sessionDtoMono.map(SessionDto::getNickname)
                .flatMap(sessionService::initSessionWithUserHavingNickname);
        return toSessionIdMessage(sessionMono);
    }

    @MessageMapping(".join.{id}")
    public Flux<Message> joinSession(@DestinationVariable String id) {
        return sessionService.joinSessionWithId(id)
                .onErrorResume(resumeWithErrorMessage());
    }

}