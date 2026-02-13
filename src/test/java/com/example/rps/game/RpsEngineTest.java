package com.example.rps.game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.rps.game.enums.Move;
import org.junit.jupiter.api.Test;

public class RpsEngineTest {
    private final RpsEngine engine = new RpsEngine();

    @Test
    void rockBeatsScissors() {
        assertEquals(RpsEngine.Outcome.WIN, engine.compare(Move.ROCK, Move.SCISSORS));
    }

    @Test
    void paperLosesToScissors() {
        assertEquals(RpsEngine.Outcome.LOSE, engine.compare(Move.PAPER, Move.SCISSORS));
    }

    @Test
    void drawWhenSame() {
        assertEquals(RpsEngine.Outcome.DRAW, engine.compare(Move.ROCK, Move.ROCK));
    }
}
