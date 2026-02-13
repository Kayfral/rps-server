package com.example.rps.game;

import static org.mockito.Mockito.verify;

import com.example.rps.game.enums.Move;
import com.example.rps.session.PlayerSession;
import com.example.rps.session.SessionManager;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GameSessionTest {

    @Mock
    private SessionManager sessionManager;

    private ScheduledExecutorService scheduler;
    private PlayerSession first;
    private PlayerSession second;
    private GameSession session;

    @BeforeEach
    void setUp() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        first = new PlayerSession("s1", "a", new EmbeddedChannel());
        second = new PlayerSession("s2", "b", new EmbeddedChannel());
        session = new GameSession(
            first,
            second,
            10,
            30,
            sessionManager,
            new RpsEngine(),
            new RoundState(),
            new RoundTimer(scheduler)
        );
    }

    @AfterEach
    void tearDown() {
        scheduler.shutdown();
    }

    @Test
    void winnerEndsGameAndRemovesSessions() {
        session.start();
        session.handleMove(first, Move.ROCK);
        session.handleMove(second, Move.SCISSORS);

        verify(sessionManager).removeSession(first);
        verify(sessionManager).removeSession(second);
    }

    @Test
    void timeoutEndsGameWhenOnlyOneMove() throws Exception {
        session = new GameSession(
            first,
            second,
            1,
            30,
            sessionManager,
            new RpsEngine(),
            new RoundState(),
            new RoundTimer(scheduler)
        );

        session.start();
        session.handleMove(first, Move.ROCK);
        Thread.sleep(1200);

        verify(sessionManager).removeSession(first);
        verify(sessionManager).removeSession(second);
    }
}
