package com.example.rps.net;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServerRunner implements ApplicationRunner {
    private final RpsServer server;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        server.start();
    }

    @PreDestroy
    public void onShutdown() {
        server.stop();
    }
}
