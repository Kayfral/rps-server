package com.example.rps.game;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

public class RoundTimerTest {

    @Test
    void schedulesTask() throws Exception {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        RoundTimer timer = new RoundTimer(scheduler);
        CountDownLatch latch = new CountDownLatch(1);

        timer.schedule(0, latch::countDown);

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        scheduler.shutdown();
    }

    @Test
    void ignoresRejectedExecutionOnShutdown() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.shutdown();
        RoundTimer timer = new RoundTimer(scheduler);

        assertDoesNotThrow(() -> timer.schedule(1, () -> {
        }));
    }
}
