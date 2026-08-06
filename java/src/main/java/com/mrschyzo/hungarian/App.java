package com.mrschyzo.hungarian;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.*;

public class App {
    public int add(int a, int b) {
        return a + b;
    }

    static void main(String[] args) throws IOException {
        var path = Path.of("players.txt");
        var random = new Random();

        try (FileChannel channel = FileChannel.open(path, CREATE, WRITE, TRUNCATE_EXISTING)) {
            for (long count = 10_000_000; count > 0; count--) {
                String x = pickSequence(random);
                channel.write(StandardCharsets.UTF_8.encode(x + System.lineSeparator()));
            }
        }

        System.out.println(Arrays.toString(args));
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
