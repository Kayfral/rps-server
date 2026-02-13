package com.example.rps.game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.rps.game.enums.Move;
import com.example.rps.game.enums.RoundResult;
import com.example.rps.session.PlayerSession;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

public class RoundStateTest {

    @Test
    void evaluateReturnsDrawWhenMovesEqual() {
        RoundState state = new RoundState();
        RpsEngine engine = new RpsEngine();
        PlayerSession first = new PlayerSession("s1", "a", new EmbeddedChannel());
        PlayerSession second = new PlayerSession("s2", "b", new EmbeddedChannel());

        state.recordMove(first, Move.ROCK);
        state.recordMove(second, Move.ROCK);

        assertEquals(RoundResult.DRAW, state.evaluate(engine, first, second));
    }

    @Test
    void evaluateReturnsWinner() {
        RoundState state = new RoundState();
        RpsEngine engine = new RpsEngine();
        PlayerSession first = new PlayerSession("s1", "a", new EmbeddedChannel());
        PlayerSession second = new PlayerSession("s2", "b", new EmbeddedChannel());

        state.recordMove(first, Move.PAPER);
        state.recordMove(second, Move.ROCK);

        assertEquals(RoundResult.FIRST_WINS, state.evaluate(engine, first, second));
    }
}
