package com.example.rps.session;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import java.util.concurrent.ScheduledFuture;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import com.example.rps.game.GameSession;

@Getter
@Setter
@RequiredArgsConstructor
public class PlayerSession {
    @NonNull
    private final String sessionId;
    @NonNull
    private final String nickname;
    @NonNull
    private volatile Channel channel;
    private volatile boolean connected = true;
    private volatile SessionState state = SessionState.NEW;
    private volatile GameSession gameSession;
    private volatile ScheduledFuture<?> reconnectTask;

    public void send(String message) {
        Channel current = this.channel;
        if (current.isActive()) {
            current.writeAndFlush(message + "\n");
        }
    }

    public void close(String message) {
        Channel current = this.channel;
        if (current.isActive()) {
            ChannelFuture future = current.writeAndFlush(message + "\n");
            future.addListener(f -> current.close());
        }
    }
}
