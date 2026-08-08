package com.mrschyzo.hungarian.interfaces;

import com.mrschyzo.hungarian.application.service.LotteryAppService;
import com.mrschyzo.hungarian.domain.Lottery;
import com.mrschyzo.hungarian.domain.LotteryLoader;
import com.mrschyzo.hungarian.domain.PickParser;
import com.mrschyzo.hungarian.domain.SimplePickParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public class IOLotteryControllerTest {
    private final PickParser parser = new SimplePickParser();

    @Test
    public void when_cli_starts_it_loads() throws IOException {
        final var loaded = new AtomicBoolean(false);
        final var out = new ByteArrayOutputStream();
        var loader = new LotteryLoader() {
            @Override
            public Lottery load() {
                loaded.set(true);
                return new Lottery();
            }
        };
        var app = new LotteryAppService(loader, parser);
        try (
            var input = new ByteArrayInputStream("exit\n".getBytes(StandardCharsets.UTF_8));
        ) {
            IOLotteryController controller = new IOLotteryController(input, out, app);
            controller.beginInteraction();
            Assertions.assertTrue(loaded.get());
            Assertions.assertTrue(out.toString().contains("READY"));
        }
    }
}
