package com.example.rps.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.rps.game.GameSession;
import com.example.rps.game.GameSessionFactory;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

@ExtendWith(MockitoExtension.class)
public class MatchmakerTest {

    @Mock
    private GameSessionFactory factory;

    @Mock
    private GameSession gameSession;

    private Matchmaker matchmaker;

    @BeforeEach
    void setUp() {
        ObjectProvider<GameSessionFactory> provider = new ObjectProvider<>() {
            @Override
            public GameSessionFactory getObject(Object... args) {
                return factory;
            }

            @Override
            public GameSessionFactory getObject() {
                return factory;
            }

            @Override
            public GameSessionFactory getIfAvailable() {
                return factory;
            }

            @Override
            public GameSessionFactory getIfUnique() {
                return factory;
            }
        };
        matchmaker = new Matchmaker(provider);
    }

    @Test
    void doesNotMatchSameSessionTwice() {
        when(factory.create(any(), any())).thenReturn(gameSession);

        PlayerSession first = new PlayerSession("s1", "a", new EmbeddedChannel());
        PlayerSession second = new PlayerSession("s2", "b", new EmbeddedChannel());

        matchmaker.enqueue(first);
        matchmaker.enqueue(first);
        matchmaker.enqueue(second);

        verify(factory).create(first, second);
        verify(gameSession).start();
        assertEquals(SessionState.IN_GAME, first.getState());
        assertEquals(SessionState.IN_GAME, second.getState());
    }
}
