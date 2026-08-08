package com.mrschyzo.hungarian.interfaces;

import com.mrschyzo.hungarian.application.service.LotteryAppService;
import com.mrschyzo.hungarian.domain.*;
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
                return new BitmaskLottery();
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
