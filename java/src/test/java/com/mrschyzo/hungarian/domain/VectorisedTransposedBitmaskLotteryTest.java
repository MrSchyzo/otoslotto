package com.mrschyzo.hungarian.domain;

public class VectorisedTransposedBitmaskLotteryTest extends LotteryTest {
    @Override
    protected Lottery getInstance() {
        return new VectorisedTransposedBitmaskLottery();
    }
}
