package com.github.vy.fibertest;

import org.junit.Test;

public class VertxRingBenchmarkTest extends VertxRingBenchmark {

    @Test
    public void testRingBenchmark() throws Exception {
        Util.testRingBenchmark(workerCount, ringSize, ringBenchmark());
    }

}
