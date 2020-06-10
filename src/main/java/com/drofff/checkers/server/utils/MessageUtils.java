package com.drofff.checkers.server.utils;

import com.drofff.checkers.server.document.Session;
import com.drofff.checkers.server.exception.ValidationException;
import com.drofff.checkers.server.message.ErrorMessage;
import com.drofff.checkers.server.message.FieldErrorsMessage;
import com.drofff.checkers.server.message.Message;
import com.drofff.checkers.server.message.TextMessage;
import reactor.core.publisher.Mono;

import java.util.function.Function;

import static com.drofff.checkers.server.utils.MapUtils.isNotEmpty;

public class MessageUtils {

    private MessageUtils() {}

    public static Mono<Message> toSessionIdMessage(Mono<Session> sessionMono) {
        return sessionMono.map(Session::getId)
                .map(TextMessage::of)
                .onErrorResume(resumeWithErrorMessage());
    }

    public static <T> Mono<Message> toTextMessage(Mono<T> mono, String messageText) {
        return mono.map(obj -> TextMessage.of(messageText))
                .onErrorResume(resumeWithErrorMessage());
    }

    public static Function<Throwable, Mono<Message>> resumeWithErrorMessage() {
        return e -> {
            Message errorMessage = isNotFieldErrorsException(e) ? ErrorMessage.of(e.getMessage()) :
                    FieldErrorsMessage.from((ValidationException) e);
            return Mono.just(errorMessage);
        };
    }

    private static boolean isNotFieldErrorsException(Throwable throwable) {
        return !isFieldErrorsException(throwable);
    }

    private static boolean isFieldErrorsException(Throwable throwable) {
        return isValidationException(throwable) && hasNonEmptyFieldErrorsMap((ValidationException) throwable);
    }

    private static boolean isValidationException(Throwable throwable) {
        return throwable instanceof ValidationException;
    }

    private static boolean hasNonEmptyFieldErrorsMap(ValidationException e) {
        return isNotEmpty(e.getFieldErrorsMap());
    }

}