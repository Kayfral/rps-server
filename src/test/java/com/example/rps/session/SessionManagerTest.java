package com.example.rps.session;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.rps.game.GameSession;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class SessionManagerTest {

    @Mock
    private Matchmaker matchmaker;

    @Mock
    private GameSession gameSession;

    @Test
    void disconnectRemovesFromWaitingQueue() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        SessionManager manager = new SessionManager(scheduler, matchmaker);
        ReflectionTestUtils.setField(manager, "reconnectWindowSeconds", 1);

        PlayerSession session = new PlayerSession("s1", "nick", new EmbeddedChannel());
        session.setState(SessionState.WAITING);

        manager.handleDisconnect(session);

        verify(matchmaker).remove(session);
        scheduler.shutdown();
    }

    @Test
    void expiresSessionAfterReconnectWindow() throws Exception {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        SessionManager manager = new SessionManager(scheduler, matchmaker);
        ReflectionTestUtils.setField(manager, "reconnectWindowSeconds", 0);

        PlayerSession session = new PlayerSession("s1", "nick", new EmbeddedChannel());
        session.setGameSession(gameSession);

        manager.handleDisconnect(session);

        Thread.sleep(50);

        verify(gameSession).onReconnectExpired(session);
        assertFalse(session.isConnected());
        scheduler.shutdown();
    }
}
