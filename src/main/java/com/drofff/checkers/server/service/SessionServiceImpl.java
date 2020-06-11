package com.drofff.checkers.server.service;

import com.drofff.checkers.server.document.*;
import com.drofff.checkers.server.enums.BoardSide;
import com.drofff.checkers.server.exception.ValidationException;
import com.drofff.checkers.server.message.Message;
import com.drofff.checkers.server.message.SessionMessage;
import com.drofff.checkers.server.message.StepMessage;
import com.drofff.checkers.server.repository.SessionRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static com.drofff.checkers.server.constants.GameConstants.BOARD_ROW_SIZE;
import static com.drofff.checkers.server.document.Piece.manOf;
import static com.drofff.checkers.server.enums.BoardSide.RED;
import static com.drofff.checkers.server.utils.ListUtils.concatenateLists;
import static com.drofff.checkers.server.utils.SecurityUtils.getCurrentUser;
import static com.drofff.checkers.server.utils.ValidationUtils.validateNotNull;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static reactor.core.publisher.Mono.error;

@Service
public class SessionServiceImpl implements SessionService {

    private static final int ONE_SIDE_PIECES_COUNT = 12;

    private final Map<String, FluxSink<Message>> fluxSinksOfUsers = new HashMap<>();

    private final SessionRepository sessionRepository;

    private final UserService userService;

    public SessionServiceImpl(SessionRepository sessionRepository, UserService userService) {
        this.sessionRepository = sessionRepository;
        this.userService = userService;
    }

    @Override
    public Mono<Session> initSessionWithUserHavingNickname(String nickname) {
        validateNotNull(nickname, "Opponent's nickname should be provided");
        return userService.getUserByNickname(nickname)
                .switchIfEmpty(error(new ValidationException("User with such a nickname doesn't exist")))
                .flatMap(this::initSessionWithOpponent)
                .flatMap(sessionRepository::save);
    }

    private Mono<Session> initSessionWithOpponent(User opponent) {
        return getCurrentUser()
                .flatMap(user -> removeSessionsHavingAnyOfUsers(user, opponent))
                .map(owner -> initSessionOfUsers(owner, opponent));
    }

    private Mono<User> removeSessionsHavingAnyOfUsers(User owner, User opponent) {
        return Flux.just(owner, opponent)
                .flatMap(this::removeSessionHavingUserIfPresent)
                .then(Mono.just(owner));
    }

    private Mono<Void> removeSessionHavingUserIfPresent(User user) {
        return getSessionOfUser(user).flatMap(sessionRepository::delete).then();
    }

    private Session initSessionOfUsers(User owner, User member) {
        Session session = new Session();
        session.setSessionOwnerId(owner.getId());
        session.setSessionMemberId(member.getId());
        Board gameBoard = initGameBoardForUsers(owner, member);
        session.setGameBoard(gameBoard);
        return session;
    }

    private Board initGameBoardForUsers(User owner, User member) {
        Board board = new Board();
        board.setTurnSide(RED);
        List<Piece> pieces = initPiecesForUsers(owner, member);
        board.setPieces(pieces);
        return board;
    }

    private List<Piece> initPiecesForUsers(User owner, User member) {
        List<Piece> piecesOfOwner = getInvertedPiecesForUser(owner);
        List<Piece> piecesOfMember = getPiecesForUser(member);
        return concatenateLists(piecesOfOwner, piecesOfMember);
    }

    private List<Piece> getInvertedPiecesForUser(User user) {
        List<Piece> userPieces = getPiecesForUser(user);
        userPieces.forEach(Piece::invertPosition);
        return userPieces;
    }

    private List<Piece> getPiecesForUser(User user) {
        return range(0, ONE_SIDE_PIECES_COUNT)
                .mapToObj(pos -> {
                    int column = columnByPosition(pos);
                    int row = rowByPosition(pos);
                    column = isOddRow(row) ? column + 1 : column;
                    return manOf(column, row, user.getId());
                }).collect(toList());
    }

    private int columnByPosition(int position) {
        return ( position * 2 ) % BOARD_ROW_SIZE;
    }

    private int rowByPosition(int position) {
        int piecesInRow = BOARD_ROW_SIZE / 2;
        return position / piecesInRow;
    }

    private boolean isOddRow(int row) {
        return row % 2 == 1;
    }

    @Override
    public Flux<Message> joinSessionWithId(String id) {
        Flux<Message> sessionFlux = getFluxOfSessionWithId(id);
        return getCurrentUser().flatMapMany(currentUser -> {
                    Consumer<FluxSink<Message>> fluxSinkConsumer = fluxSink -> saveFluxSinkForUser(fluxSink, currentUser);
                    return Flux.create(fluxSinkConsumer);
                }).mergeWith(sessionFlux).doOnComplete(finishSessionWithId(id));
    }

    private Flux<Message> getFluxOfSessionWithId(String id) {
        return sessionRepository.findById(id)
                .switchIfEmpty(error(new ValidationException("Session with such id doesn't exist")))
                .flatMap(this::validateUserIsMemberOfSession)
                .flatMapMany(SessionMessage::of);
    }

    private Mono<Session> validateUserIsMemberOfSession(Session session) {
        return getCurrentUser()
                .filter(session::hasUser)
                .switchIfEmpty(error(new SecurityException("User should be a session member to join it")))
                .thenReturn(session);
    }

    private void saveFluxSinkForUser(FluxSink<Message> fluxSink, User user) {
        String userId = user.getId();
        completeFluxSinkWithKeyIfPresent(userId);
        fluxSinksOfUsers.put(userId, fluxSink);
    }

    private void completeFluxSinkWithKeyIfPresent(String userId) {
        FluxSink<Message> fluxSink = fluxSinksOfUsers.get(userId);
        Optional.ofNullable(fluxSink).ifPresent(FluxSink::complete);
    }

    private Runnable finishSessionWithId(String id) {
        return () -> sessionRepository.findById(id).flatMap(session -> {
            List<String> sessionMembersIds = asList(session.getSessionOwnerId(), session.getSessionMemberId());
            removeFluxSinkOfUsersWithId(sessionMembersIds);
            return sessionRepository.delete(session);
        }).subscribe();
    }

    private void removeFluxSinkOfUsersWithId(List<String> userIds) {
        fluxSinksOfUsers.entrySet().stream()
                .filter(userFluxSink -> userIds.contains(userFluxSink.getKey()))
                .forEach(userFluxSink -> userFluxSink.getValue().complete());
    }

    @Override
    public Mono<Void> sendStepToOpponent(Step step) {
        return getOpponentId().flatMap(opponentId -> getBoardSideOfUser(getCurrentUser()).doOnNext(userSide -> {
            StepMessage stepMessage = new StepMessage(userSide, step);
            sendMessageToUserWithId(stepMessage, opponentId);
        })).then();
    }

    private Mono<String> getOpponentId() {
        return getCurrentSession().flatMap(session -> getCurrentUser().map(currentUser ->
                session.isOwnedBy(currentUser) ? session.getSessionMemberId() : session.getSessionOwnerId())
        );
    }

    @Override
    public Mono<Void> sendStepToSessionMembers(Step step) {
        return getCurrentSession().doOnNext(session -> {
            sendStepToSessionOwner(step, session);
            sendStepToSessionMember(step, session);
        }).then();
    }

    private void sendStepToSessionOwner(Step step, Session session) {
        String ownerId = session.getSessionOwnerId();
        StepMessage stepMessage = new StepMessage(BoardSide.RED, step);
        sendMessageToUserWithId(stepMessage, ownerId);
    }

    private void sendStepToSessionMember(Step step, Session session) {
        String memberId = session.getSessionMemberId();
        StepMessage stepMessage = new StepMessage(BoardSide.BLACK, step);
        sendMessageToUserWithId(stepMessage, memberId);
    }

    @Override
    public void sendMessageToUserWithId(Message message, String userId) {
        fluxSinksOfUsers.get(userId).next(message);
    }

    @Override
    public Mono<Session> getCurrentSession() {
        return getCurrentUser().flatMap(this::getSessionOfUser);
    }

    @Override
    public Mono<BoardSide> getBoardSideOfUser(Mono<User> userMono) {
        return userMono.flatMap(user -> getSessionOfUser(user).map(session ->
                session.isOwnedBy(user) ? BoardSide.RED : BoardSide.BLACK));
    }

    @Override
    public Mono<Session> getSessionOfUser(User user) {
        return sessionRepository.findBySessionOwnerIdOrSessionMemberId(user.getId(), user.getId());
    }

    @Override
    public Mono<Void> updateSession(Session session) {
        return sessionRepository.save(session).then();
    }

    @Override
    public Mono<Void> removeSession(Session session) {
        return sessionRepository.delete(session);
    }

}