package com.mrschyzo.hungarian.application.service;

import com.mrschyzo.hungarian.domain.Lottery;
import com.mrschyzo.hungarian.domain.LotteryLoader;
import com.mrschyzo.hungarian.domain.pick.Pick;
import com.mrschyzo.hungarian.domain.pick.PickParser;

import java.util.Map;

public class LotteryAppService {
    private final LotteryLoader loader;
    private PickParser parser;
    private Lottery lottery;
    private ThreadLocal<Pick> pickThreadLocal = ThreadLocal.withInitial(() -> Pick.of(1, 2, 3, 4, 5));
    public LotteryAppService(LotteryLoader loader, PickParser parser) {
        this.loader = loader;
        this.parser = parser;
    }

    public void load() throws ApplicationException {
        try {
            lottery = loader.load();
        } catch (Exception e) {
            throw new LotteryCannotBeLoadedException(e);
        }
    }

    public Map<Integer, Integer> query(String rawPick) {
        if (lottery == null) {
            return Map.of(2,0,3,0,4,0,5,0);
        }
        try {
            var pick = pickThreadLocal.get();
            parser.parse(rawPick, pick);
            var winners = lottery.getWinnersHistogram(pick);
            return Map.of(
                    2, winners.get2MatchCount(),
                    3, winners.get3MatchCount(),
                    4, winners.get4MatchCount(),
                    5, winners.get5MatchCount()
            );
        } catch (Exception e) {
            System.err.println(e);
            return Map.of(2,0,3,0,4,0,5,0);
        }
    }
}
