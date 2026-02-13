package com.example.rps.game;

import com.example.rps.game.enums.Move;
import org.springframework.stereotype.Component;

@Component
public class RpsEngine {
    public Outcome compare(Move first, Move second) {
        if (first == second) {
            return Outcome.DRAW;
        }
        return switch (first) {
            case ROCK -> (second == Move.SCISSORS) ? Outcome.WIN : Outcome.LOSE;
            case SCISSORS -> (second == Move.PAPER) ? Outcome.WIN : Outcome.LOSE;
            case PAPER -> (second == Move.ROCK) ? Outcome.WIN : Outcome.LOSE;
        };
    }

    public enum Outcome {
        WIN,
        LOSE,
        DRAW
    }
}
