package com.example.rps.game;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.rps.game.enums.Move;
import org.junit.jupiter.api.Test;

public class GameMessagesTest {

    @Test
    void movePromptIncludesChoicesAndSeconds() {
        String prompt = GameMessages.movePrompt(7);
        assertTrue(prompt.contains("7"));
        assertTrue(prompt.contains("Rock"));
        assertTrue(prompt.contains("Scissors"));
        assertTrue(prompt.contains("Paper"));
    }

    @Test
    void formatMoveReturnsEnglishNames() {
        assertTrue(GameMessages.formatMove(Move.ROCK).contains("Rock"));
        assertTrue(GameMessages.formatMove(Move.SCISSORS).contains("Scissors"));
        assertTrue(GameMessages.formatMove(Move.PAPER).contains("Paper"));
    }
}
