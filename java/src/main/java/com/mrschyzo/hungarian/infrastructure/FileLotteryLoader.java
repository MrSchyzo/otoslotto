package com.mrschyzo.hungarian.infrastructure;

import com.mrschyzo.hungarian.domain.*;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileLotteryLoader implements LotteryLoader {
    private PickParser parser;
    private Path path;

    public FileLotteryLoader(PickParser parser, Path path) {
        this.parser = parser;
        this.path = path;
    }

    public Lottery load() {
        var lottery = new BitmaskLottery(10_000_000);
        try(var lines = Files.lines(path)) {
            lines.forEach(x -> parseAndAccept(x, lottery));
            return lottery;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void parseAndAccept(String rawPick, Lottery lottery) {
        try {
            lottery.acceptPick(parser.parse(rawPick));
        } catch (Exception _) {}
    }
}
