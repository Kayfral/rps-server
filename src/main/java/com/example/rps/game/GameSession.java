package com.example.rps.game;

import com.example.rps.game.enums.Move;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import com.example.rps.game.enums.RoundResult;
import com.example.rps.session.PlayerSession;
import com.example.rps.session.SessionManager;

@RequiredArgsConstructor
public class GameSession {
    @NonNull
    private final PlayerSession first;
    @NonNull
    private final PlayerSession second;
    private final int moveTimeoutSeconds;
    private final int reconnectWindowSeconds;
    @NonNull
    private final SessionManager sessionManager;
    @NonNull
    private final RpsEngine engine;
    @NonNull
    private final RoundState roundState;
    @NonNull
    private final RoundTimer roundTimer;

    private final Object lock = new Object();
    private boolean ended;
    private boolean paused;

    public void start() {
        first.send("Opponent found: " + second.getNickname());
        second.send("Opponent found: " + first.getNickname());
        startRound("The match begins!");
    }

    public void handleMove(PlayerSession player, Move move) {
        synchronized (lock) {
            if (ended || paused) {
                player.send("The game is not available right now.");
                return;
            }

            if (roundState.hasMove(player)) {
                player.send("You already made a choice. Waiting for the opponent.");
                return;
            }

            roundState.recordMove(player, move);

            if (roundState.count() < 2) {
                player.send("Choice accepted. Waiting for the opponent.");
                return;
            }

            roundTimer.cancel();
            resolveRound();
        }
    }

    public void onDisconnect(PlayerSession player) {
        PlayerSession opponent = opponentOf(player);
        synchronized (lock) {
            if (ended) {
                return;
            }

            paused = true;
            roundTimer.cancel();
            if (opponent != null) {
                opponent.send("Player " + player.getNickname() + " disconnected. "
                    + "Waiting for reconnect for " + reconnectWindowSeconds + " seconds.");
            }
        }
    }

    public void onReconnect(PlayerSession player) {
        PlayerSession opponent = opponentOf(player);
        synchronized (lock) {
            if (ended) {
                player.send("The game is already finished.");
                return;
            }

            paused = false;
            if (opponent != null) {
                opponent.send("Player " + player.getNickname() + " is back. The round will restart.");
            }
            startRound("The round will restart.");
        }
    }

    public void onReconnectExpired(PlayerSession player) {
        PlayerSession opponent = opponentOf(player);
        synchronized (lock) {
            if (ended) {
                return;
            }

            ended = true;
            roundTimer.cancel();
            if (opponent != null && opponent.isConnected()) {
                opponent.close("Player " + player.getNickname() + " did not reconnect. Game over.");
            }

            if (opponent != null) {
                sessionManager.removeSession(opponent);
            }
        }
    }

    private void resolveRound() {
        RoundResult result = roundState.evaluate(engine, first, second);
        if (result == RoundResult.INCOMPLETE) {
            return;
        }

        if (result == RoundResult.DRAW) {
            first.send("Draw. The round restarts.");
            second.send("Draw. The round restarts.");
            startRound("Make a new choice.");
            return;
        }

        PlayerSession winner = (result == RoundResult.FIRST_WINS) ? first : second;
        PlayerSession loser = (winner == first) ? second : first;

        winner.close("You win! Your choice: " + GameMessages.formatMove(roundState.getMove(winner))
            + ". Opponent choice: " + GameMessages.formatMove(roundState.getMove(loser)) + ".");
        loser.close("You lose. Your choice: " + GameMessages.formatMove(roundState.getMove(loser))
            + ". Opponent choice: " + GameMessages.formatMove(roundState.getMove(winner)) + ".");

        ended = true;
        sessionManager.removeSession(first);
        sessionManager.removeSession(second);
    }

    private void startRound(String intro) {
        roundState.clear();
        if (ended || paused) {
            return;
        }

        if (intro != null && !intro.isBlank()) {
            first.send(intro);
            second.send(intro);
        }

        String prompt = GameMessages.movePrompt(moveTimeoutSeconds);
        first.send(prompt);
        second.send(prompt);

        roundTimer.schedule(moveTimeoutSeconds, this::onRoundTimeout);
    }

    private void onRoundTimeout() {
        synchronized (lock) {
            if (ended || paused) {
                return;
            }

            if (roundState.count() == 0) {
                first.send("Time is up. The round will restart.");
                second.send("Time is up. The round will restart.");
                startRound(null);
                return;
            }

            if (roundState.count() == 1) {
                PlayerSession winner = roundState.hasMove(first) ? first : second;
                PlayerSession loser = (winner == first) ? second : first;
                winner.close("Opponent did not make a choice in time. You win.");
                loser.close("You did not make a choice in time. You lose.");
                ended = true;
                sessionManager.removeSession(first);
                sessionManager.removeSession(second);
            }
        }
    }

    private PlayerSession opponentOf(PlayerSession player) {
        if (player == first) {
            return second;
        }

        if (player == second) {
            return first;
        }

        return null;
    }

}
