package com.mrschyzo.hungarian.domain;

import com.mrschyzo.hungarian.domain.pick.Pick;

import java.util.Arrays;

public class BitmaskLottery implements Lottery {
    private int count;
    private int[] pickBitMasks;

    public BitmaskLottery(){
        count = 0;
        pickBitMasks = new int[3];
    }

    public BitmaskLottery(int capacity){
        count = 0;
        pickBitMasks = new int[3 * capacity];
    }

    @Override
    public void acceptPick(Pick pick) {
        var bitmask = pick.toBitmask();
        if (count * 3 >= pickBitMasks.length) {
            pickBitMasks = Arrays.copyOf(pickBitMasks, pickBitMasks.length * 2);
        }
        System.arraycopy(bitmask, 0, pickBitMasks, count * 3, 3);
        count++;
    }

    @Override
    public Histogram getWinnersHistogram(Pick winningOne) {
        var matchHistogram = new int[]{0,0,0,0,0,0};
        var referenceMask = winningOne.toBitmask();
        for (int i = 0; i < count * 3; i += 3) {
            var hitCount =
                    Integer.bitCount(pickBitMasks[i] & referenceMask[0]) +
                    Integer.bitCount(pickBitMasks[i+1] & referenceMask[1]) +
                    Integer.bitCount(pickBitMasks[i+2] & referenceMask[2]);
            matchHistogram[hitCount]++;
        }
        return new Histogram(matchHistogram);
    }
}
