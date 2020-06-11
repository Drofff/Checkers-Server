package com.drofff.checkers.server.service;

import com.drofff.checkers.server.document.User;
import com.drofff.checkers.server.repository.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static com.drofff.checkers.server.utils.ValidationUtils.validateNotNull;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<User> getUserById(String id) {
        validateNotNull(id, "Id is required");
        return userRepository.findById(id);
    }

    @Override
    public Mono<User> getUserByNickname(String nickname) {
        validateNotNull(nickname, "Nickname is required");
        return userRepository.findByNickname(nickname);
    }

    @Override
    public Mono<User> getUserByEmail(String email) {
        validateNotNull(email, "Email should be provided");
        return userRepository.findByEmail(email);
    }

    @Override
    public Mono<Void> updateUser(User user) {
        validateNotNull(user, "User should be provided");
        validateNotNull(user.getId(), "User should obtain an id");
        return userRepository.save(user).then();
    }

}