package com.mrschyzo.hungarian.infrastructure;

import com.mrschyzo.hungarian.domain.Lottery;
import com.mrschyzo.hungarian.domain.LotteryLoader;
import com.mrschyzo.hungarian.domain.PickParser;

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
        var lottery = new Lottery();
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
