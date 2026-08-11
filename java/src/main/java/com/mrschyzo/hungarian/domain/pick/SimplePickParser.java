package com.mrschyzo.hungarian.domain.pick;

import java.util.Arrays;
import java.util.stream.IntStream;

public class SimplePickParser implements PickParser {
    private ThreadLocal<int[]> picks = ThreadLocal.withInitial(() -> new int[]{1,2,3,4,5});
    public SimplePickParser() {}

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

    @Override
    public void parse(String str, Pick pick) {
        var numbers = this.picks.get();
        var i = 0;
        var c = 0;
        var currentNumber = 0;
        var invalid = false;
        var base = 10;
        while (i < numbers.length) {
            while (c < str.length()) {
                var character = str.charAt(c++);
                if (character >= '0' && character <= '9') {
                    currentNumber = currentNumber * base + (character - '0');
                } else if (character == ' ' && currentNumber > 0 && !invalid) {
                    numbers[i++] = currentNumber;
                    currentNumber = 0;
                    break;
                } else {
                    currentNumber = 0;
                    invalid = character != ' ';
                }
            }
            if (currentNumber > 0 && i < numbers.length) {
                numbers[i++] = currentNumber;
                currentNumber = 0;
                invalid = false;
            } else if (c >= str.length()) {
                throw new IllegalArgumentException(String.format("Not enough numbers for a pick, expecting 5, got %d", i));
            }
        }
        pick.reset(numbers[0], numbers[1], numbers[2], numbers[3], numbers[4]);
    }
}
