package com.example.rps.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class SchedulerConfigTest {

    @Test
    void createsScheduler() {
        SchedulerConfig config = new SchedulerConfig();
        assertNotNull(config.gameScheduler());
    }
}
