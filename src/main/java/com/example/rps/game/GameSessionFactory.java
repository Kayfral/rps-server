package com.example.rps.game;

import java.util.concurrent.ScheduledExecutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.example.rps.session.PlayerSession;
import com.example.rps.session.SessionManager;

@Component
@RequiredArgsConstructor
public class GameSessionFactory {
    private final ScheduledExecutorService scheduler;
    private final SessionManager sessionManager;
    private final RpsEngine engine;

    @Value("${rps.server.move-timeout-seconds}")
    private int moveTimeoutSeconds;

    @Value("${rps.server.reconnect-window-seconds}")
    private int reconnectWindowSeconds;

    public GameSession create(PlayerSession first, PlayerSession second) {
        return new GameSession(first, second, moveTimeoutSeconds, reconnectWindowSeconds, sessionManager, engine,
            new RoundState(), new RoundTimer(scheduler));
    }
}
