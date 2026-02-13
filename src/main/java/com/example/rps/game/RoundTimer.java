package com.example.rps.game;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class RoundTimer {
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> timeoutTask;

    public RoundTimer(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    public void schedule(int seconds, Runnable task) {
        cancel();
        try {
            timeoutTask = scheduler.schedule(task, seconds, TimeUnit.SECONDS);
        } catch (RejectedExecutionException ignored) {
            timeoutTask = null;
        }
    }

    public void cancel() {
        if (timeoutTask != null) {
            timeoutTask.cancel(false);
            timeoutTask = null;
        }
    }
}
