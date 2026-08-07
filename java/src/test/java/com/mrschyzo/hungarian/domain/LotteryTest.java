package com.mrschyzo.hungarian.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LotteryTest {
    @Test
    public void lottery_can_accept_picks() {
        Lottery lottery = new Lottery();
        Assertions.assertDoesNotThrow(() -> lottery.acceptPick(Pick.of(1, 2, 3, 4, 5)));
    }

    @Test
    public void lottery_returns_zeroed_histogram_when_no_picks_have_been_added() {
        Lottery lottery = new Lottery();
        Lottery.Histogram result = lottery.getWinnersHistogram(Pick.of(1, 2, 3, 4, 5));
        Assertions.assertEquals(0, result.get2MatchCount());
        Assertions.assertEquals(0, result.get3MatchCount());
        Assertions.assertEquals(0, result.get4MatchCount());
        Assertions.assertEquals(0, result.get5MatchCount());
    }

    @Test
    public void lottery_returns_zeroed_histogram_when_no_picks_match_at_least_2_numbers() {
        Lottery lottery = new Lottery();
        lottery.acceptPick(Pick.of(10, 20, 30, 40, 50));
        Lottery.Histogram result = lottery.getWinnersHistogram(Pick.of(10, 2, 3, 4, 5));
        Assertions.assertEquals(0, result.get2MatchCount());
        Assertions.assertEquals(0, result.get3MatchCount());
        Assertions.assertEquals(0, result.get4MatchCount());
        Assertions.assertEquals(0, result.get5MatchCount());
    }

    @Test
    public void lottery_returns_non_zero_histogram_when_a_pick_matches_at_least_2_numbers() {
        Lottery lottery = new Lottery();
        lottery.acceptPick(Pick.of(1, 2, 3, 4, 5));
        Lottery.Histogram result = lottery.getWinnersHistogram(Pick.of(1, 2, 30, 40, 50));
        Assertions.assertEquals(1, result.get2MatchCount());
        Assertions.assertEquals(0, result.get3MatchCount());
        Assertions.assertEquals(0, result.get4MatchCount());
        Assertions.assertEquals(0, result.get5MatchCount());
    }

    @Test
    public void lottery_returns_the_highest_number_of_match_for_the_picks() {
        Lottery lottery = new Lottery();
        lottery.acceptPick(Pick.of(1, 2, 3, 4, 5));
        Lottery.Histogram result = lottery.getWinnersHistogram(Pick.of(1, 2, 3, 4, 50));
        Assertions.assertEquals(0, result.get2MatchCount());
        Assertions.assertEquals(0, result.get3MatchCount());
        Assertions.assertEquals(1, result.get4MatchCount());
        Assertions.assertEquals(0, result.get5MatchCount());
    }

    @Test
    public void lottery_does_not_care_about_ordering_when_matching() {
        Lottery lottery = new Lottery();
        lottery.acceptPick(Pick.of(1, 2, 3, 4, 5));
        Lottery.Histogram result = lottery.getWinnersHistogram(Pick.of(3, 2, 1, 4, 5));
        Assertions.assertEquals(0, result.get2MatchCount());
        Assertions.assertEquals(0, result.get3MatchCount());
        Assertions.assertEquals(0, result.get4MatchCount());
        Assertions.assertEquals(1, result.get5MatchCount());
    }

    @Test
    public void lottery_consider_same_combinations_as_distinct_even_regardless_of_order() {
        Lottery lottery = new Lottery();
        lottery.acceptPick(Pick.of(1, 2, 3, 4, 5));
        lottery.acceptPick(Pick.of(1, 2, 3, 4, 5));
        lottery.acceptPick(Pick.of(5, 4, 3, 2, 1));
        Lottery.Histogram result = lottery.getWinnersHistogram(Pick.of(3, 2, 1, 4, 5));
        Assertions.assertEquals(0, result.get2MatchCount());
        Assertions.assertEquals(0, result.get3MatchCount());
        Assertions.assertEquals(0, result.get4MatchCount());
        Assertions.assertEquals(3, result.get5MatchCount());
    }
}
