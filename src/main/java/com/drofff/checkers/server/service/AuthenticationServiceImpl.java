package com.drofff.checkers.server.service;

import com.drofff.checkers.server.document.User;
import com.drofff.checkers.server.exception.SecurityException;
import com.drofff.checkers.server.exception.ValidationException;
import com.drofff.checkers.server.repository.UserRepository;
import com.drofff.checkers.server.type.Mail;
import com.drofff.checkers.server.type.UserActivation;
import com.drofff.checkers.server.utils.SecurityUtils;
import org.springframework.data.util.Pair;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.drofff.checkers.server.utils.MailUtils.getActivationMail;
import static com.drofff.checkers.server.utils.ValidationUtils.validate;
import static com.drofff.checkers.server.utils.ValidationUtils.validateNotNull;
import static reactor.core.publisher.Mono.error;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;

    private final MailService mailService;

    private final PasswordEncoder passwordEncoder;

    public AuthenticationServiceImpl(UserRepository userRepository, MailService mailService,
                                     PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<User> authenticate(Mono<Pair<String, String>> emailAndPasswordMono) {
        return emailAndPasswordMono.doOnNext(this::validateEmailAndPassword)
                .flatMap(this::getUserByEmailAndPassword)
                .switchIfEmpty(error(new SecurityException("Invalid user credentials")))
                .doOnNext(SecurityUtils::setCurrentUser);
    }

    private void validateEmailAndPassword(Pair<String, String> emailAndPassword) {
        validateNotNull(emailAndPassword.getFirst(), "Email should be provided");
        validateNotNull(emailAndPassword.getSecond(), "Password is required");
    }

    private Mono<User> getUserByEmailAndPassword(Pair<String, String> emailAndPassword) {
        return userRepository.findByEmail(emailAndPassword.getFirst())
                .filter(user -> isPasswordOfUser(emailAndPassword.getSecond(), user));
    }

    private boolean isPasswordOfUser(String password, User user) {
        return passwordEncoder.matches(password, user.getPassword());
    }

    @Override
    public Mono<Void> registerUser(Mono<User> userMono) {
        return userMono.flatMap(this::validateUser)
                .doOnNext(this::preProcessUser)
                .flatMap(userRepository::save)
                .flatMap(this::sendActivationTokenToUser);
    }

    private Mono<User> validateUser(User user) {
        validate(user);
        return validateUserHasUniqueEmailAndNickname(user);
    }

    private Mono<User> validateUserHasUniqueEmailAndNickname(User user) {
        return userRepository.findByNickname(user.getNickname())
                .flatMap(usr -> error(new ValidationException("Nickname is already in use")))
                .switchIfEmpty(Mono.just(user))
                .flatMap(usr -> userRepository.findByEmail(user.getEmail()))
                .flatMap(usr -> error(new ValidationException("User with such an email already exists")))
                .switchIfEmpty(Mono.just(user))
                .thenReturn(user);
    }

    private void preProcessUser(User user) {
        encodeUserPassword(user);
        user.setActive(false);
        String randomToken = UUID.randomUUID().toString();
        user.setActivationToken(randomToken);
    }

    private void encodeUserPassword(User user) {
        String rawPassword = user.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);
    }

    private Mono<Void> sendActivationTokenToUser(User user) {
        Mail activationMail = getActivationMail(user.getNickname(), user.getActivationToken());
        return mailService.sendMailTo(activationMail, user.getEmail());
    }

    @Override
    public Mono<Void> activateUser(Mono<UserActivation> userActivationMono) {
        return userActivationMono
                .flatMap(this::validateUserActivation)
                .flatMap(this::markUserAsActive);
    }

    private Mono<User> validateUserActivation(UserActivation userActivation) {
        validate(userActivation);
        return getUserById(userActivation.getUserId())
                .filter(user -> user.hasActivationToken(userActivation.getToken()))
                .switchIfEmpty(error(new ValidationException("Invalid activation token")));
    }

    private Mono<User> getUserById(String id) {
        return userRepository.findById(id)
                .switchIfEmpty(error(new ValidationException("User with such id doesn't exist")));
    }

    private Mono<Void> markUserAsActive(User user) {
        user.setActive(true);
        return userRepository.save(user).then();
    }

}