package com.mrschyzo.hungarian.application.service;

import com.mrschyzo.hungarian.domain.Lottery;
import com.mrschyzo.hungarian.domain.LotteryLoader;
import com.mrschyzo.hungarian.domain.Pick;

import java.util.Map;

public class LotteryAppService {
    private LotteryLoader loader;
    private Lottery lottery;
    public LotteryAppService(LotteryLoader loader) {
        this.loader = loader;
    }

    public void load() throws ApplicationException {
        try {
            lottery = loader.load();
        } catch (Exception e) {
            throw new LotteryCannotBeLoadedException(e);
        }
    }

    public Map<Integer, Integer> query(int[] pick) {
        if (lottery == null || pick.length < 5) {
            return Map.of(2,0,3,0,4,0,5,0);
        }
        try {
            var winners = lottery.getWinnersHistogram(Pick.of(pick[0], pick[1], pick[2], pick[3], pick[4]));
            return Map.of(
                    2, winners.get2MatchCount(),
                    3, winners.get3MatchCount(),
                    4, winners.get4MatchCount(),
                    5, winners.get5MatchCount()
            );
        } catch (Exception _) {
            return Map.of(2,0,3,0,4,0,5,0);
        }
    }
}
