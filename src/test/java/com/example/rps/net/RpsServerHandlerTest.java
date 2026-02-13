package com.example.rps.net;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.rps.session.Matchmaker;
import com.example.rps.session.PlayerSession;
import com.example.rps.session.SessionManager;
import com.example.rps.session.SessionState;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class RpsServerHandlerTest {

    @Mock
    private SessionManager sessionManager;

    @Mock
    private Matchmaker matchmaker;

    @InjectMocks
    private RpsServerHandler handler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(handler, "maxNickLength", 100);
    }

    @Test
    void greetsOnConnect() {
        EmbeddedChannel channel = new EmbeddedChannel(handler);
        Object first = channel.readOutbound();
        Object second = channel.readOutbound();

        assertTrue(first.toString().contains("Welcome"));
        assertTrue(second.toString().contains("Enter a nickname"));
    }

    @Test
    void createsSessionOnNickname() {
        EmbeddedChannel channel = new EmbeddedChannel(handler);
        PlayerSession session = new PlayerSession("s1", "nick", channel);
        when(sessionManager.createSession(any(), any())).thenReturn(session);

        channel.writeInbound("nick");

        Object outbound;
        boolean hasSession = false;
        while ((outbound = channel.readOutbound()) != null) {
            if (outbound.toString().contains("Your SESSION")) {
                hasSession = true;
                break;
            }
        }

        assertTrue(hasSession);
        verify(matchmaker).enqueue(session);
    }

    @Test
    void restoresSessionWhenProvided() {
        EmbeddedChannel channel = new EmbeddedChannel(handler);
        PlayerSession session = new PlayerSession("s1", "nick", channel);
        session.setState(SessionState.WAITING);
        when(sessionManager.restoreSession("s1", channel)).thenReturn(Optional.of(session));

        channel.writeInbound("SESSION s1");

        verify(matchmaker).enqueue(session);
    }
}
