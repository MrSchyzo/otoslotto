package com.mrschyzo.hungarian;

import com.mrschyzo.hungarian.domain.*;
import com.mrschyzo.hungarian.domain.pick.Pick;
import org.openjdk.jmh.annotations.*;

import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class MyBenchmark {
    private Lottery lottery = new VectorisedTransposedBitmaskLottery(10_000_000);
    //private Lottery lottery = new BitmaskLottery(10_000_000);
    private Pick pick = Pick.of(1, 2, 3, 4, 5);
    private Random random = new Random();

    @Setup(Level.Trial)
    public void setUp() {
        var set = HashSet.<Integer>newHashSet(5);
        for(int i = 0; i < 10_000_000; i++) {
            while(set.size() < 5) {
                set.add(random.nextInt(1, 91));
            }
            var rawPick = set.stream().mapToInt(x -> x).toArray();
            lottery.acceptPick(Pick.of(rawPick[0], rawPick[1], rawPick[2], rawPick[3], rawPick[4]));
            set.clear();
        }
    }

    @Benchmark
    public BitmaskLottery.Histogram query() {
        return lottery.getWinnersHistogram(pick);
    }
}
