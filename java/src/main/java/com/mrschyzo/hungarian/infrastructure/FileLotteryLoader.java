package com.mrschyzo.hungarian.infrastructure;

import com.mrschyzo.hungarian.domain.*;
import com.mrschyzo.hungarian.domain.pick.Pick;
import com.mrschyzo.hungarian.domain.pick.PickParser;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileLotteryLoader implements LotteryLoader {
    private PickParser parser;
    private Path path;
    private Pick pick = Pick.of(1, 2, 3, 4, 5);

    public FileLotteryLoader(PickParser parser, Path path) {
        this.parser = parser;
        this.path = path;
    }

    public Lottery load() {
        var lottery = new VectorisedTransposedBitmaskLottery(10_000_000);
        try(var lines = Files.lines(path)) {
            lines.forEach(x -> parseAndAccept(x, lottery));
            return lottery;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void parseAndAccept(String rawPick, Lottery lottery) {
        try {
            parser.parse(rawPick, pick);
            lottery.acceptPick(pick);
        } catch (Exception _) {}
    }
}
