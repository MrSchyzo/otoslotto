package com.mrschyzo.hungarian.application.service;

public class LotteryCannotBeLoadedException extends ApplicationException {
    public LotteryCannotBeLoadedException(Exception e) {
        super(e);
    }
}
