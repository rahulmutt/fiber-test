This project bundles a set of benchmarks that aims to measure the performance
of a multitude of threading implementations in the Java Virtual Machine.

Benchmarks
----------

As of now, there is just a single benchmark (ring) adopted from the
[Performance Measurements of Threads in Java and Processes in
Erlang](http://www.sics.se/~joe/ericsson/du98024.html) article. I plan to
introduce new benchmarks as time allows. That being said, contributions are
welcome.

Implementations
---------------

Project employs 4 threading libraries to implement fibers in the benchmarks.

1. [Standard Java Threads](http://docs.oracle.com/javase/7/docs/api/java/lang/Thread.html)
2. [Akka Actors](http://akka.io/)
3. [Quasar Fibers and Actors](http://docs.paralleluniverse.co/quasar/)
4. [Eta Fibers](https://github.com/rahulmutt/eta-fibers-dev)

Usage
-----

1. Install [Eta](http://eta-lang.org/docs/html/getting-started.html#method-1-binary-installation).

2. Install `eta-fibers-dev`.

```
git clone https://github.com/rahulmutt/eta-fibers-dev
cd eta-fibers-dev
etlas install
```

3. Run `./install.sh`

4. Run `mvn clean install`

Next, you can either use the provided `benchmark.sh` script

    $ ./benchmark.sh --help
    Available parameters (with defaults):
        workerCount (503)
        ringSize    (1000000)
        cpuList     (0-1)

    # You can run with default parameters.
    $ ./benchmarks.sh

    # Alternatively, you can configure parameters through environment variables.
    $ ringSize=500 workerCount=30 cpuList=0-7 ./benchmark.sh

or call JMH manually:

    $ java \
    > -jar target/fiber-test.jar \
    > -jvmArgsAppend "-DworkerCount=503 -DringSize=1000000 -javaagent:/path/to/quasar-core-<version>.jar" \
    > -wi 5 -i 10 -bm avgt -tu ms -f 5 \
    > ".*RingBenchmark.*"

Instead of using JMH, you can additionally use `assembly:single` goal to
create an all-in-one JAR and run benchmarks individually.

    $ mvn assembly:single
    $ java \
    > -server -XX:+TieredCompilation -XX:+AggressiveOpts \
    > -DworkerCount=503 -DringSize=10000000 \
    > -javaagent:/path/to/quasar-core-<version>.jar \
    > -cp target/fiber-test-<version>-jar-with-dependencies.jar \
    > com.github.vy.fibertest.QuasarFiberRingBenchmark

Results
-------

For `Oracle Java 1.8.0_102-b14` running on `Intel(R) Core(TM) i5-5250U CPU @ 1.60GHz` on `OS X El Capitan Version 10.11.5`:

```
AkkaActorRingBenchmark.ringBenchmark       avgt   50   657.889 ±  23.338  ms/op
EtaFiberRingBenchmark.ringBenchmark        avgt   50   949.181 ±  51.674  ms/op
JavaThreadRingBenchmark.ringBenchmark      avgt   50  9803.600 ± 177.944  ms/op
QuasarActorRingBenchmark.ringBenchmark     avgt   50  2678.167 ± 122.796  ms/op
QuasarChannelRingBenchmark.ringBenchmark   avgt   50  2099.612 ± 123.330  ms/op
QuasarDataflowRingBenchmark.ringBenchmark  avgt   50  2296.656 ± 139.150  ms/op
QuasarFiberRingBenchmark.ringBenchmark     avgt   50   950.884 ±  98.654  ms/op
```

License
-------

The [fiber-test](https://github.com/vy/fiber-test/) by [Volkan Yazıcı](http://vlkan.com/) is licensed under the [Creative Commons Attribution 4.0 International License](http://creativecommons.org/licenses/by/4.0/).

[![Creative Commons Attribution 4.0 International License](http://i.creativecommons.org/l/by/4.0/80x15.png)](http://creativecommons.org/licenses/by/4.0/)
