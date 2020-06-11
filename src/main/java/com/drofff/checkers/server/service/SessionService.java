package com.drofff.checkers.server.service;

import com.drofff.checkers.server.document.Session;
import com.drofff.checkers.server.document.Step;
import com.drofff.checkers.server.document.User;
import com.drofff.checkers.server.enums.BoardSide;
import com.drofff.checkers.server.message.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SessionService {

    Mono<Session> initSessionWithUserHavingNickname(String nickname);

    Flux<Message> joinSessionWithId(String id);

    Mono<Void> updateSession(Session session);

    Mono<Void> sendStepToOpponent(Step step);

    Mono<Void> sendStepToSessionMembers(Step step);

    void sendMessageToUserWithId(Message message, String userId);

    Mono<Session> getCurrentSession();

    Mono<BoardSide> getBoardSideOfUser(Mono<User> userMono);

    Mono<Session> getSessionOfUser(User user);

    Mono<Void> removeSession(Session session);

}