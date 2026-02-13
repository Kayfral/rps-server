package com.example.rps.game;

import com.example.rps.game.enums.Move;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GameMessages {

    public static String movePrompt(int seconds) {
        return "Your choice (" + seconds + " seconds):\n"
            + "[1] Rock\n"
            + "[2] Scissors\n"
            + "[3] Paper\n"
            + "Enter a number or a word.";
    }

    public static String formatMove(Move move) {
        return switch (move) {
            case ROCK -> "Rock";
            case SCISSORS -> "Scissors";
            case PAPER -> "Paper";
        };
    }
}
