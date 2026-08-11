package com.mrschyzo.hungarian;

import com.mrschyzo.hungarian.application.service.LotteryAppService;
import com.mrschyzo.hungarian.domain.pick.SimplePickParser;
import com.mrschyzo.hungarian.infrastructure.FileLotteryLoader;
import com.mrschyzo.hungarian.interfaces.IOLotteryController;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Random;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.*;

public class App {
    private static final Path path = Path.of("players.txt");
    private static final int TOTAL = 10_000_000;

    public int add(int a, int b) {
        return a + b;
    }

    static void main(String[] args) throws Exception {
        if (args.length > 0) {
            interactiveMode();
            return;
        }
        generateTickets();
    }

    private static void interactiveMode() throws Exception {
        var parser = new SimplePickParser();
        var path = Path.of("players.txt");
        var loader = new FileLotteryLoader(parser, path);
        var service = new LotteryAppService(loader, parser);
        try(var controller = new IOLotteryController(System.in, System.out, service)) {
            controller.beginInteraction();
        }
    }

    private static void generateTickets() throws IOException {
        var random = new Random();
        System.out.println("Generation mode");

        try (FileChannel channel = FileChannel.open(path, CREATE, WRITE, TRUNCATE_EXISTING)) {
            for (long count = 0; count < TOTAL; count++) {
                String x = pickSequence(random);
                channel.write(StandardCharsets.UTF_8.encode(x + System.lineSeparator()));
                if (count % 256*1024 == 0) {
                    System.out.printf("\r\033[2KProgress: %d/%d", count, TOTAL);
                    System.out.flush();
                }
            }
        }
        System.out.println("\r\033[2KDone");
    }

    private static String pickSequence(Random random) {
        var picked = new HashSet<Integer>(5);
        while(picked.size() < 5){
            var next = random.nextInt(1, 91);
            picked.add(next);
        }
        return picked.stream().map(Object::toString).collect(Collectors.joining(" "));
    }
}
