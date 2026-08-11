package com.mrschyzo.hungarian.domain;

import com.mrschyzo.hungarian.domain.pick.Pick;

import java.util.Arrays;

public class TransposedBitmaskLottery implements Lottery {
    private int count;
    private final long[][] matchingBitmasks;

    public TransposedBitmaskLottery() {
        this(0);
    }

    public TransposedBitmaskLottery(int initialCapacity) {
        count = 0;
        matchingBitmasks = new long[90][initialCapacity / 64];
    }

    @Override
    public void acceptPick(Pick pick) {
        if (count / 64 >= matchingBitmasks[0].length) {
            for (int i = 0; i < 90; i++) {
                long[] bitmask = matchingBitmasks[i];
                var newLength = (bitmask.length == 0 ? 1 : bitmask.length * 2);
                matchingBitmasks[i] = Arrays.copyOf(bitmask, newLength);
            }
        }
        for (int number : pick.getRawData()) {
            getMask(number)[count / 64] |= 1L << (63 - count % 64);
        }
        count++;
    }

    @Override
    public Histogram getWinnersHistogram(Pick winningOne) {
        var matchHistogram = new int[]{0, 0, 0, 0, 0, 0};
        var pick = winningOne.getRawData();
        var bitmask0 = getMask(pick[0]);
        var bitmask1 = getMask(pick[1]);
        var bitmask2 = getMask(pick[2]);
        var bitmask3 = getMask(pick[3]);
        var bitmask4 = getMask(pick[4]);
        var words = (count + 63) / 64;
        var combinedMatchSum = new long[3];

        // The gist of it is to do 64 tickets at the time: each bit in the bitmask represents a ticket
        for (int i = 0; i < words; i++) {
            // Parallel 64b bitwise sum out of 5 tickets, each ticket can output at most 3-bit integers, from 000 to 101
            bitwiseCarrySumAdd(bitmask0[i], bitmask1[i], bitmask2[i], bitmask3[i], bitmask4[i], combinedMatchSum);

            long high = combinedMatchSum[2];
            long mid = combinedMatchSum[1];
            long low = combinedMatchSum[0];

            // Popcount and f(h,m,l) tell me which tickets respect the expected amount of hits represented by a binary combination
            matchHistogram[2] += Long.bitCount(  mid & ~low);
            matchHistogram[3] += Long.bitCount(mid &  low);
            matchHistogram[4] += Long.bitCount( high & ~low);
            matchHistogram[5] += Long.bitCount( high & low);
        }

        return new Histogram(matchHistogram);
    }

    private long[] getMask(int number) {
        return matchingBitmasks[number - 1];
    }

    private void bitwiseCarrySumAdd(long bits1, long bits2, long bits3, long bits4, long bits5, long[] result) {
        long sumLow = bitwiseSum(bits1, bits2, bits3);
        long carryLow = bitwiseCarry(bits1, bits2, bits3);

        long sumMid = bitwiseSum(bits4, bits5);
        long carryMid = bitwiseCarry(bits4, bits5);
        long carryHigh = bitwiseCarry(sumLow, sumMid);

        result[0] = bitwiseSum(sumLow, sumMid);
        result[1] = bitwiseSum(carryLow, carryMid, carryHigh);
        result[2] = bitwiseCarry(carryLow, carryMid, carryHigh);
    }

    private long bitwiseCarry(long bits1, long bits2, long bits3) {
        return (bits1 & bits2) | (bits1 & bits3) | (bits2 & bits3);
    }

    private long bitwiseSum(long bits1, long bits2, long bits3) {
        return bits1 ^ bits2 ^ bits3;
    }

    private long bitwiseCarry(long bits1, long bits2) {
        return bits1 & bits2;
    }

    private long bitwiseSum(long bits1, long bits2) {
        return bits1 ^ bits2;
    }
}
