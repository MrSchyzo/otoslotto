package com.mrschyzo.hungarian.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

abstract class LotteryTest {
    private Lottery lottery;
    protected abstract Lottery getInstance();

    @BeforeEach
    public final void setUp() {
        this.lottery = getInstance();
    }

    @Test
    public final void lottery_can_accept_picks() {
        Assertions.assertDoesNotThrow(() -> lottery.acceptPick(Pick.of(1, 2, 3, 4, 5)));
    }

    @Test
    public final void lottery_returns_zeroed_histogram_when_no_picks_have_been_added() {
        BitmaskLottery.Histogram result = lottery.getWinnersHistogram(Pick.of(1, 2, 3, 4, 5));
        Assertions.assertEquals(0, result.get2MatchCount());
        Assertions.assertEquals(0, result.get3MatchCount());
        Assertions.assertEquals(0, result.get4MatchCount());
        Assertions.assertEquals(0, result.get5MatchCount());
    }

    @Test
    public final void lottery_returns_zeroed_histogram_when_no_picks_match_at_least_2_numbers() {
        lottery.acceptPick(Pick.of(10, 20, 30, 40, 50));
        BitmaskLottery.Histogram result = lottery.getWinnersHistogram(Pick.of(10, 2, 3, 4, 5));
        Assertions.assertEquals(0, result.get2MatchCount());
        Assertions.assertEquals(0, result.get3MatchCount());
        Assertions.assertEquals(0, result.get4MatchCount());
        Assertions.assertEquals(0, result.get5MatchCount());
    }

    @Test
    public final void lottery_returns_non_zero_histogram_when_a_pick_matches_at_least_2_numbers() {
        lottery.acceptPick(Pick.of(1, 2, 3, 4, 5));
        BitmaskLottery.Histogram result = lottery.getWinnersHistogram(Pick.of(1, 2, 30, 40, 50));
        Assertions.assertEquals(1, result.get2MatchCount());
        Assertions.assertEquals(0, result.get3MatchCount());
        Assertions.assertEquals(0, result.get4MatchCount());
        Assertions.assertEquals(0, result.get5MatchCount());
    }

    @Test
    public final void lottery_returns_the_highest_number_of_match_for_the_picks() {
        lottery.acceptPick(Pick.of(1, 2, 3, 4, 5));
        BitmaskLottery.Histogram result = lottery.getWinnersHistogram(Pick.of(1, 2, 3, 4, 50));
        Assertions.assertEquals(0, result.get2MatchCount());
        Assertions.assertEquals(0, result.get3MatchCount());
        Assertions.assertEquals(1, result.get4MatchCount());
        Assertions.assertEquals(0, result.get5MatchCount());
    }

    @Test
    public final void lottery_does_not_care_about_ordering_when_matching() {
        lottery.acceptPick(Pick.of(1, 2, 3, 4, 5));
        BitmaskLottery.Histogram result = lottery.getWinnersHistogram(Pick.of(3, 2, 1, 4, 5));
        Assertions.assertEquals(0, result.get2MatchCount());
        Assertions.assertEquals(0, result.get3MatchCount());
        Assertions.assertEquals(0, result.get4MatchCount());
        Assertions.assertEquals(1, result.get5MatchCount());
    }

    @Test
    public final void lottery_consider_same_combinations_as_distinct_even_regardless_of_order() {
        lottery.acceptPick(Pick.of(1, 2, 3, 4, 5));
        lottery.acceptPick(Pick.of(1, 2, 3, 4, 5));
        lottery.acceptPick(Pick.of(5, 4, 3, 2, 1));
        BitmaskLottery.Histogram result = lottery.getWinnersHistogram(Pick.of(3, 2, 1, 4, 5));
        Assertions.assertEquals(0, result.get2MatchCount());
        Assertions.assertEquals(0, result.get3MatchCount());
        Assertions.assertEquals(0, result.get4MatchCount());
        Assertions.assertEquals(3, result.get5MatchCount());
    }
}
