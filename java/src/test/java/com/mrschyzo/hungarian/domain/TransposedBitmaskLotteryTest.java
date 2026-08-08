package com.mrschyzo.hungarian.domain;

public class TransposedBitmaskLotteryTest extends LotteryTest {
    @Override
    protected Lottery getInstance() {
        return new TransposedBitmaskLottery();
    }
}
