package com.mrschyzo.hungarian.domain;

import java.util.Arrays;
import java.util.stream.IntStream;

public class SimplePickParser implements PickParser {
    public SimplePickParser() {
    }

    public Pick parse(String str) {
        var split = Arrays
                .stream(str.split(" "))
                .flatMapToInt(x -> {
                    try {
                        return IntStream.of(Integer.parseInt(x));
                    } catch (Exception e) {
                        return IntStream.empty();
                    }
                })
                .toArray();

        if (split.length < 5)
            throw new IllegalArgumentException(String.format("Not enough numbers for a pick, expecting 5, got %d", split.length));
        return Pick.of(split[0], split[1], split[2], split[3], split[4]);
    }
}
