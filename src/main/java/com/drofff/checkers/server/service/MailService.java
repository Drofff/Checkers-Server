package com.drofff.checkers.server.service;

import com.drofff.checkers.server.type.Mail;
import reactor.core.publisher.Mono;

public interface MailService {

    Mono<Void> sendMailTo(Mail mail, String ... receivers);

}
