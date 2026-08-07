package com.mrschyzo.hungarian.domain;

public class Pick {
    private int[] picks;

    private Pick(int[] entries) {
        picks = entries;
    }

    public int[] toBitmask() {
        var result = new int[] {0,0,0};
        for (int pick : picks) {
            var bit = pick - 1;
            result[bit / 32] |= 1 << (31 - bit % 32);
        }
        return result;
    }

    public static Pick of(int value1, int value2, int value3, int value4, int value5) {
        var values = new int[]{value1, value2, value3, value4, value5};
        for(int i = 0; i < values.length; i++) {
            var current = values[i];
            if (current < 1 || current > 90)
                throw new IllegalArgumentException(String.format("Value at index %d has to be between 1 and 90", i));
            for (int j = 0; j < values.length; j++) {
                var sentinel = values[j];
                if (i != j && current == sentinel) {
                    throw new IllegalArgumentException(String.format("Value at index %d and index %d should not be equal", i, j));
                }
            }
        }
        return new Pick(new int[]{value1, value2, value3, value4, value5});
    }
}
