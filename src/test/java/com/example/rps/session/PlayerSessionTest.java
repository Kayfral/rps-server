package com.example.rps.session;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

public class PlayerSessionTest {

    @Test
    void sendWritesToChannel() {
        EmbeddedChannel channel = new EmbeddedChannel();
        PlayerSession session = new PlayerSession("s1", "nick", channel);

        session.send("hello");

        Object outbound = channel.readOutbound();
        assertTrue(outbound.toString().contains("hello"));
    }

    @Test
    void closeClosesChannel() {
        EmbeddedChannel channel = new EmbeddedChannel();
        PlayerSession session = new PlayerSession("s1", "nick", channel);

        session.close("bye");
        channel.runPendingTasks();

        assertFalse(channel.isActive());
    }
}
