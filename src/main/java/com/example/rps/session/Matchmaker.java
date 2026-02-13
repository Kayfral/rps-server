package com.example.rps.session;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import com.example.rps.game.GameSession;
import com.example.rps.game.GameSessionFactory;

@Component
@RequiredArgsConstructor
public class Matchmaker {
    private final Queue<PlayerSession> waiting = new ConcurrentLinkedQueue<>();
    private final Set<String> waitingIds = ConcurrentHashMap.newKeySet();
    private final ObjectProvider<GameSessionFactory> gameSessionFactory;

    public void enqueue(PlayerSession session) {
        session.setState(SessionState.WAITING);
        if (waitingIds.add(session.getSessionId())) {
            waiting.add(session);
            tryMatch();
        }
    }

    public void remove(PlayerSession session) {
        if (waitingIds.remove(session.getSessionId())) {
            waiting.remove(session);
        }
    }

    private synchronized void tryMatch() {
        while (true) {
            PlayerSession first = pollWaiting();
            PlayerSession second = pollWaiting();

            if (first == null || second == null) {
                if (first != null) {
                    requeue(first);
                }
                return;
            }

            if (!first.isConnected() || !second.isConnected()) {
                if (first.isConnected()) {
                    requeue(first);
                }

                if (second.isConnected()) {
                    requeue(second);
                }

                continue;
            }

            GameSession gameSession = gameSessionFactory.getObject().create(first, second);
            first.setGameSession(gameSession);
            second.setGameSession(gameSession);
            first.setState(SessionState.IN_GAME);
            second.setState(SessionState.IN_GAME);
            gameSession.start();
        }
    }

    private PlayerSession pollWaiting() {
        PlayerSession session = waiting.poll();
        if (session != null) {
            waitingIds.remove(session.getSessionId());
        }
        return session;
    }

    private void requeue(PlayerSession session) {
        if (waitingIds.add(session.getSessionId())) {
            waiting.add(session);
        }
    }
}
