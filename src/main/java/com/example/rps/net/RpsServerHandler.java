package com.example.rps.net;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.example.rps.game.enums.Move;
import com.example.rps.session.Matchmaker;
import com.example.rps.session.PlayerSession;
import com.example.rps.session.SessionManager;
import com.example.rps.session.SessionState;

@Component
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class RpsServerHandler extends SimpleChannelInboundHandler<String> {
    private static final AttributeKey<PlayerSession> SESSION_KEY = AttributeKey.valueOf("playerSession");

    private final SessionManager sessionManager;
    private final Matchmaker matchmaker;

    @Value("${rps.server.max-nick-length}")
    private int maxNickLength;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.writeAndFlush("Welcome to Rock-Paper-Scissors!\n");
        ctx.writeAndFlush("Enter a nickname (up to " + maxNickLength + " characters) "
            + "or SESSION <id> to restore.\n");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        String input = msg.trim();
        if (input.isEmpty()) {
            return;
        }

        PlayerSession session = ctx.channel().attr(SESSION_KEY).get();
        if (session == null) {
            handleAuthentication(ctx, input);
            return;
        }

        if (session.getState() == SessionState.WAITING) {
            session.send("Waiting for an opponent...");
            return;
        }

        if (session.getState() == SessionState.IN_GAME) {
            Move move = parseMove(input);
            if (move == null) {
                session.send("Invalid input. Enter 1/2/3 or Rock/Scissors/Paper.");
                return;
            }

            session.getGameSession().handleMove(session, move);
            return;
        }

        session.send("Session ended.");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        sessionManager.findByChannel(ctx.channel()).ifPresent(sessionManager::handleDisconnect);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }

    private void handleAuthentication(ChannelHandlerContext ctx, String input) {
        String upper = input.toUpperCase(Locale.ROOT);
        if (upper.startsWith("SESSION ")) {
            String sessionId = input.substring("SESSION ".length()).trim();
            if (sessionId.isEmpty()) {
                ctx.writeAndFlush("Provide SESSION <id>.\n");
                return;
            }

            Optional<PlayerSession> restored = sessionManager.restoreSession(sessionId, ctx.channel());
            if (restored.isEmpty()) {
                ctx.writeAndFlush("Session not found or expired.\n");
                return;
            }

            PlayerSession session = restored.get();
            ctx.channel().attr(SESSION_KEY).set(session);
            session.send("Session restored. Nickname: " + session.getNickname());

            if (session.getState() == SessionState.WAITING) {
                matchmaker.enqueue(session);
                session.send("Waiting for an opponent...");
            } else if (session.getState() == SessionState.IN_GAME && session.getGameSession() != null) {
                session.getGameSession().onReconnect(session);
            } else {
                session.send("Session ended.");
            }

            return;
        }

        String nickname = input.trim();
        if (nickname.isEmpty() || nickname.length() > maxNickLength) {
            ctx.writeAndFlush("Invalid nickname. Try again.\n");
            return;
        }

        PlayerSession session = sessionManager.createSession(nickname, ctx.channel());
        ctx.channel().attr(SESSION_KEY).set(session);
        session.send("Your SESSION: " + session.getSessionId());
        session.send("Waiting for an opponent...");
        matchmaker.enqueue(session);
    }

    private Move parseMove(String input) {
        String normalized = input.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "1", "rock" -> Move.ROCK;
            case "2", "scissors" -> Move.SCISSORS;
            case "3", "paper" -> Move.PAPER;
            default -> null;
        };
    }
}
