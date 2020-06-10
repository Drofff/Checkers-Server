package com.drofff.checkers.server.controller;

import com.drofff.checkers.server.document.User;
import com.drofff.checkers.server.dto.AuthenticationDto;
import com.drofff.checkers.server.dto.UserActivationDto;
import com.drofff.checkers.server.dto.UserDto;
import com.drofff.checkers.server.mapper.UserActivationDtoMapper;
import com.drofff.checkers.server.mapper.UserDtoMapper;
import com.drofff.checkers.server.message.Message;
import com.drofff.checkers.server.service.AuthenticationService;
import com.drofff.checkers.server.type.UserActivation;
import org.springframework.data.util.Pair;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static com.drofff.checkers.server.utils.MessageUtils.toTextMessage;

@RestController
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    private final UserDtoMapper userDtoMapper;
    private final UserActivationDtoMapper userActivationDtoMapper;

    public AuthenticationController(AuthenticationService authenticationService, UserDtoMapper userDtoMapper,
                                    UserActivationDtoMapper userActivationDtoMapper) {
        this.authenticationService = authenticationService;
        this.userDtoMapper = userDtoMapper;
        this.userActivationDtoMapper = userActivationDtoMapper;
    }

    @MessageMapping("login")
    public Mono<Message> authenticate(@Payload Mono<AuthenticationDto> authenticationDtoMono) {
        Mono<Pair<String, String>> emailAndPasswordMono = authenticationDtoMono
                .map(authDto -> Pair.of(authDto.getEmail(), authDto.getPassword()));
        Mono<User> userMono = authenticationService.authenticate(emailAndPasswordMono);
        return toTextMessage(userMono, "Successfully authenticated user");
    }

    @MessageMapping("account.register")
    public Mono<Message> registerAccount(@Payload Mono<UserDto> userDtoMono) {
        Mono<User> userMono = userDtoMono.map(userDtoMapper::toEntity);
        Mono<Void> mono = authenticationService.registerUser(userMono);
        return toTextMessage(mono,
                "Registration has been successfully completed. Activate your account");
    }

    @MessageMapping("account.activate")
    public Mono<Message> activateAccount(@Payload Mono<UserActivationDto> userActivationDtoMono) {
        Mono<UserActivation> userActivationMono = userActivationDtoMono.map(userActivationDtoMapper::toEntity);
        Mono<Void> mono = authenticationService.activateUser(userActivationMono);
        return toTextMessage(mono, "Account has been successfully activated");
    }

}