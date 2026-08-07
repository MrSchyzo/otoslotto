package com.mrschyzo.hungarian.application.service;

import com.mrschyzo.hungarian.domain.LotteryLoader;

public class LotteryAppService {
    private LotteryLoader loader;
    public LotteryAppService(LotteryLoader loader) {
        this.loader = loader;
    }

    public void load() throws ApplicationException {
        try {
            loader.load();
        } catch (Exception e) {
            throw new LotteryCannotBeLoadedException(e);
        }
    }
}
