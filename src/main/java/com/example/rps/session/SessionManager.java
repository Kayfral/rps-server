package com.example.rps.session;

import io.netty.channel.Channel;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.example.rps.game.GameSession;

@Component
@RequiredArgsConstructor
public class SessionManager {
    private final Map<String, PlayerSession> sessions = new ConcurrentHashMap<>();
    private final Map<Channel, PlayerSession> channelToSession = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler;
    private final Matchmaker matchmaker;

    @Value("${rps.server.reconnect-window-seconds}")
    private int reconnectWindowSeconds;

    public PlayerSession createSession(String nickname, Channel channel) {
        String sessionId = UUID.randomUUID().toString();
        PlayerSession session = new PlayerSession(sessionId, nickname, channel);
        sessions.put(sessionId, session);
        channelToSession.put(channel, session);
        return session;
    }

    public Optional<PlayerSession> findByChannel(Channel channel) {
        return Optional.ofNullable(channelToSession.get(channel));
    }

    public Optional<PlayerSession> restoreSession(String sessionId, Channel channel) {
        PlayerSession session = sessions.get(sessionId);
        if (session == null) {
            return Optional.empty();
        }

        ScheduledFuture<?> reconnectTask = session.getReconnectTask();
        if (reconnectTask != null) {
            reconnectTask.cancel(false);
            session.setReconnectTask(null);
        }

        Channel oldChannel = session.getChannel();
        if (oldChannel != channel) {
            channelToSession.remove(oldChannel);
            if (oldChannel.isActive()) {
                oldChannel.close();
            }
        }

        session.setChannel(channel);
        session.setConnected(true);
        channelToSession.put(channel, session);
        return Optional.of(session);
    }

    public void handleDisconnect(PlayerSession session) {
        session.setConnected(false);
        channelToSession.remove(session.getChannel());

        if (session.getState() == SessionState.WAITING) {
            matchmaker.remove(session);
        }

        ScheduledFuture<?> reconnectTask = scheduler.schedule(() -> expireSession(session),
            reconnectWindowSeconds, TimeUnit.SECONDS);
        session.setReconnectTask(reconnectTask);

        GameSession gameSession = session.getGameSession();
        if (gameSession != null) {
            gameSession.onDisconnect(session);
        }
    }

    public void removeSession(PlayerSession session) {
        ScheduledFuture<?> reconnectTask = session.getReconnectTask();
        if (reconnectTask != null) {
            reconnectTask.cancel(false);
            session.setReconnectTask(null);
        }

        sessions.remove(session.getSessionId());
        channelToSession.remove(session.getChannel());
    }

    private void expireSession(PlayerSession session) {
        if (session.isConnected()) {
            return;
        }

        GameSession gameSession = session.getGameSession();
        if (gameSession != null) {
            gameSession.onReconnectExpired(session);
        }

        removeSession(session);
    }
}
