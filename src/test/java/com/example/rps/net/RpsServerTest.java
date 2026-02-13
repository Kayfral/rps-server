package com.example.rps.net;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.example.rps.session.Matchmaker;
import com.example.rps.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class RpsServerTest {

    @Mock
    private SessionManager sessionManager;

    @Mock
    private Matchmaker matchmaker;

    private RpsServer server;

    @BeforeEach
    void setUp() {
        RpsServerHandler handler = new RpsServerHandler(sessionManager, matchmaker);
        ReflectionTestUtils.setField(handler, "maxNickLength", 100);

        server = new RpsServer(handler);
        ReflectionTestUtils.setField(server, "port", 0);
        ReflectionTestUtils.setField(server, "maxLineLength", 256);
    }

    @Test
    void startsAndStops() {
        assertDoesNotThrow(() -> {
            server.start();
            server.stop();
        });
    }
}
