package com.mrschyzo.hungarian.domain;

import java.util.Arrays;

public class Lottery {
    private int count;
    private int[] pickBitMasks;

    public Lottery(){
        count = 0;
        pickBitMasks = new int[3];
    }

    public void acceptPick(Pick pick) {
        var bitmask = pick.toBitmask();
        if (count * 3 >= pickBitMasks.length) {
            pickBitMasks = Arrays.copyOf(pickBitMasks, pickBitMasks.length * 2);
        }
        System.arraycopy(bitmask, 0, pickBitMasks, count * 3, 3);
        count++;
    }

    public Histogram getWinnersHistogram(Pick winningOne) {
        var matchHistogram = new int[]{0,0,0,0,0,0};
        var referenceMask = winningOne.toBitmask();
        for (int i = 0; i < pickBitMasks.length; i += 3) {
            var hitCount =
                    Integer.bitCount(pickBitMasks[i] & referenceMask[0]) +
                    Integer.bitCount(pickBitMasks[i+1] & referenceMask[1]) +
                    Integer.bitCount(pickBitMasks[i+2] & referenceMask[2]);
            matchHistogram[hitCount]++;
        }
        return new Histogram(matchHistogram);
    }

    public static class Histogram {
        private int[] hits;
        private Histogram(int[] hits) {
            this.hits = hits;
        }

        public int get2MatchCount() {
            return hits[2];
        }

        public int get3MatchCount() {
            return hits[3];
        }

        public int get4MatchCount() {
            return hits[4];
        }

        public int get5MatchCount() {
            return hits[5];
        }
    }
}
