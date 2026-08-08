package com.mrschyzo.hungarian.domain;

public class BitmaskLotteryTest extends LotteryTest {
    @Override
    protected Lottery getInstance() {
        return new BitmaskLottery();
    }
}
