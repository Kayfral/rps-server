package com.example.rps.game;

import java.util.HashMap;
import java.util.Map;

import com.example.rps.game.enums.Move;
import com.example.rps.game.enums.RoundResult;
import com.example.rps.session.PlayerSession;

public class RoundState {
    private final Map<PlayerSession, Move> moves = new HashMap<>();

    public boolean hasMove(PlayerSession player) {
        return moves.containsKey(player);
    }

    public void recordMove(PlayerSession player, Move move) {
        moves.put(player, move);
    }

    public int count() {
        return moves.size();
    }

    public Move getMove(PlayerSession player) {
        return moves.get(player);
    }

    public void clear() {
        moves.clear();
    }

    public RoundResult evaluate(RpsEngine engine, PlayerSession first, PlayerSession second) {
        Move firstMove = moves.get(first);
        Move secondMove = moves.get(second);
        if (firstMove == null || secondMove == null) {
            return RoundResult.INCOMPLETE;
        }

        RpsEngine.Outcome outcome = engine.compare(firstMove, secondMove);
        return switch (outcome) {
            case DRAW -> RoundResult.DRAW;
            case WIN -> RoundResult.FIRST_WINS;
            case LOSE -> RoundResult.SECOND_WINS;
        };
    }
}
