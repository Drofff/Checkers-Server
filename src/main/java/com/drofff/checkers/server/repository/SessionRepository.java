package com.drofff.checkers.server.repository;

import com.drofff.checkers.server.document.Session;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface SessionRepository extends ReactiveCrudRepository<Session, String> {

    Mono<Session> findBySessionOwnerIdOrSessionMemberId(String ownerId, String memberId);

}