package com.mrschyzo.hungarian.infrastructure;

import com.mrschyzo.hungarian.domain.Lottery;
import com.mrschyzo.hungarian.domain.pick.Pick;
import com.mrschyzo.hungarian.domain.pick.SimplePickParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Objects;

public class FileLotteryLoaderTest {
    @Test
    public void parse_lottery_file_successfully() throws URISyntaxException {
        Path path = resource("simple_few_lines.txt");
        FileLotteryLoader loader = new FileLotteryLoader(new SimplePickParser(), path);
        Lottery lottery = loader.load();
        var result = lottery.getWinnersHistogram(Pick.of(10, 20, 30, 40, 50));
        Assertions.assertEquals(2, result.get2MatchCount());
        Assertions.assertEquals(1, result.get3MatchCount());
        Assertions.assertEquals(1, result.get4MatchCount());
        Assertions.assertEquals(1, result.get5MatchCount());
    }

    @Test
    public void lottery_parsing_ignores_broken_lines() throws URISyntaxException {
        Path path = resource("some_broken_lines.txt");
        FileLotteryLoader loader = new FileLotteryLoader(new SimplePickParser(), path);
        Lottery lottery = loader.load();
        var result = lottery.getWinnersHistogram(Pick.of(10, 20, 30, 40, 50));
        Assertions.assertEquals(2, result.get2MatchCount());
        Assertions.assertEquals(1, result.get3MatchCount());
        Assertions.assertEquals(1, result.get4MatchCount());
        Assertions.assertEquals(1, result.get5MatchCount());
    }

    private Path resource(String name) throws URISyntaxException {
        return Path.of(
                Objects.requireNonNull(
                        getClass()
                                .getClassLoader()
                                .getResource(name)
                ).toURI()
        );
    }
}
