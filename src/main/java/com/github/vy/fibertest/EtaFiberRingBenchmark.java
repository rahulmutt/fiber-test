package com.github.vy.fibertest;

import org.openjdk.jmh.annotations.Benchmark;

import eta.threadring.ThreadRing;

/**
 * Ring benchmark using Java threads.
 */
public class EtaFiberRingBenchmark extends AbstractRingBenchmark {

    @Override
    @Benchmark
    public int[] ringBenchmark() throws Exception {
        return ThreadRing.start(workerCount, ringSize);
    }

    public static void main(String[] args) throws Exception {
        new EtaFiberRingBenchmark().ringBenchmark();
    }

}
