package com.mrschyzo.hungarian.domain;

import jdk.incubator.vector.LongVector;
import jdk.incubator.vector.VectorSpecies;

import java.util.Arrays;

import static jdk.incubator.vector.VectorOperators.AND;
import static jdk.incubator.vector.VectorOperators.NOT;
import static jdk.incubator.vector.VectorOperators.OR;
import static jdk.incubator.vector.VectorOperators.XOR;

public class VectorisedTransposedBitmaskLottery implements Lottery {
    private static final VectorSpecies<Long> SPECIES = LongVector.SPECIES_PREFERRED;
    private int count;
    private final long[][] matchingBitmasks;

    public VectorisedTransposedBitmaskLottery() {
        this(0);
    }

    public VectorisedTransposedBitmaskLottery(int initialCapacity) {
        count = 0;
        matchingBitmasks = new long[90][initialCapacity / 64];
    }

    @Override
    public void acceptPick(Pick pick) {
        if (count / 64 >= matchingBitmasks[0].length) {
            for (int i = 0; i < 90; i++) {
                long[] bitmask = matchingBitmasks[i];
                int newLength = bitmask.length == 0 ? 1 : bitmask.length * 2;
                matchingBitmasks[i] = Arrays.copyOf(bitmask, newLength);
            }
        }

        int word = count / 64;
        long bit = 1L << (63 - count % 64);

        for (int number : pick.getRawData()) {
            matchingBitmasks[number - 1][word] |= bit;
        }

        count++;
    }

    @Override
    public Histogram getWinnersHistogram(Pick winningOne) {
        int[] matchHistogram = new int[6];

        int[] pick = winningOne.getRawData();

        long[] bitmask0 = getMask(pick[0]);
        long[] bitmask1 = getMask(pick[1]);
        long[] bitmask2 = getMask(pick[2]);
        long[] bitmask3 = getMask(pick[3]);
        long[] bitmask4 = getMask(pick[4]);

        int words = (count + 63) / 64;
        int lanes = SPECIES.length();

        int i = 0;

        for (; i + lanes <= words; i += lanes) {
            LongVector a = LongVector.fromArray(SPECIES, bitmask0, i);
            LongVector b = LongVector.fromArray(SPECIES, bitmask1, i);
            LongVector c = LongVector.fromArray(SPECIES, bitmask2, i);
            LongVector d = LongVector.fromArray(SPECIES, bitmask3, i);
            LongVector e = LongVector.fromArray(SPECIES, bitmask4, i);

            LongVector sumLow = a.lanewise(XOR, b).lanewise(XOR, c);
            LongVector carryLow = a.lanewise(AND, b)
                    .lanewise(OR, a.lanewise(AND, c))
                    .lanewise(OR, b.lanewise(AND, c));

            LongVector sumMid = d.lanewise(XOR, e);
            LongVector carryMid = d.lanewise(AND, e);

            LongVector carryHigh = sumLow.lanewise(AND, sumMid);

            LongVector low = sumLow.lanewise(XOR, sumMid);
            LongVector mid = carryLow.lanewise(XOR, carryMid).lanewise(XOR, carryHigh);
            LongVector high = carryLow.lanewise(AND, carryMid)
                    .lanewise(OR, carryLow.lanewise(AND, carryHigh))
                    .lanewise(OR, carryMid.lanewise(AND, carryHigh));

            /*
             * 2 = 010 -> mid & ~low
             * 3 = 011 -> mid &  low
             * 4 = 100 -> high & ~low
             * 5 = 101 -> high &  low
             */
            LongVector notLow = low.lanewise(NOT);
            LongVector match2 = mid.lanewise(AND, notLow);
            LongVector match3 = mid.lanewise(AND, low);
            LongVector match4 = high.lanewise(AND, notLow);
            LongVector match5 = high.lanewise(AND, low);

            for (int lane = 0; lane < lanes; lane++) {
                matchHistogram[2] += Long.bitCount(match2.lane(lane));
                matchHistogram[3] += Long.bitCount(match3.lane(lane));
                matchHistogram[4] += Long.bitCount(match4.lane(lane));
                matchHistogram[5] += Long.bitCount(match5.lane(lane));
            }
        }

        for (; i < words; i++) {
            long a = bitmask0[i];
            long b = bitmask1[i];
            long c = bitmask2[i];
            long d = bitmask3[i];
            long e = bitmask4[i];

            long sumLow = a ^ b ^ c;
            long carryLow = (a & b) | (a & c) | (b & c);

            long sumMid = d ^ e;
            long carryMid = d & e;

            long carryHigh = sumLow & sumMid;

            long low = sumLow ^ sumMid;
            long mid = carryLow ^ carryMid ^ carryHigh;
            long high = (carryLow & carryMid) | (carryLow & carryHigh) | (carryMid & carryHigh);

            matchHistogram[2] += Long.bitCount(mid & ~low);
            matchHistogram[3] += Long.bitCount(mid & low);
            matchHistogram[4] += Long.bitCount(high & ~low);
            matchHistogram[5] += Long.bitCount(high & low);
        }

        return new Histogram(matchHistogram);
    }

    private long[] getMask(int number) {
        return matchingBitmasks[number - 1];
    }
}
