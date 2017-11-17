package com.github.vy.fibertest;

import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import org.openjdk.jmh.annotations.Benchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by bartek on 18.09.15.
 */
public class VertxRingBenchmark extends AbstractRingBenchmark {

    public static final String RESULTS_ADDRESS = "results";

    public static class WorkerVerticle extends AbstractVerticle {

        private String myId;
        private String nextWorker;

        @Override
        public void start() throws Exception {
            myId = config().getInteger("id").toString();
            nextWorker = config().getInteger("next").toString();
            MessageConsumer<Integer> cc = vertx.eventBus().consumer(myId);
            cc.handler(event -> {
                int sequence = event.body();
                if (sequence < 1) {
                    vertx.eventBus().send(RESULTS_ADDRESS, sequence,
                            new DeliveryOptions().addHeader("id", myId));
                    cc.unregister();
                }
                vertx.eventBus().send(nextWorker, sequence - 1);
            });
        }
    }

    @Override
    @Benchmark
    public int[] ringBenchmark() throws Exception {

        Vertx vertx = Vertx.vertx();

        List<CompletableFuture<Object>> deployFutures = new ArrayList<>();

        for (int i = 0; i < workerCount; i++) {
            JsonObject deployConfig = new JsonObject();
            deployConfig.put("id", i);
            deployConfig.put("next", i == workerCount - 1 ? 0 : i + 1);

            CompletableFuture<Object> fut = new CompletableFuture<Object>();
            deployFutures.add(fut);
            vertx.deployVerticle(WorkerVerticle.class.getName(),
                    new DeploymentOptions().setConfig(deployConfig), deployHdl -> {
                        fut.complete(null);
                    });
        }

        final int[] sequences = new int[workerCount];
        CountDownLatch collected = new CountDownLatch(workerCount);

        vertx.eventBus().consumer(RESULTS_ADDRESS, new Handler<Message<Integer>>() {
            @Override
            public void handle(Message<Integer> event) {
                int workerId = Integer.parseInt(event.headers().get("id"));
                sequences[workerId] = event.body();
                collected.countDown();
            }
        });

        CompletableFuture.allOf((CompletableFuture<Object>[]) deployFutures.toArray(new CompletableFuture[0]))
            .thenAccept(cons ->
            {
                vertx.eventBus().send("0", ringSize);
            });

        Awaitility.waitAtMost(Duration.TEN_MINUTES).pollDelay(1, TimeUnit.MILLISECONDS).until(() -> collected.getCount() == 0);
        return sequences;
    }

    public static void main(String[] args) throws Exception {
        new VertxRingBenchmark().ringBenchmark();
    }
}
