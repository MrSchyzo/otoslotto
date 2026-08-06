package com.mrschyzo.hungarian;

import org.openjdk.jmh.annotations.Benchmark;

public class MyBenchmark {

    @Benchmark
    public int sum() {
        return 1 + 2 ;
    }
}
