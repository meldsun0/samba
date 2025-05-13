package samba.async;

import org.hyperledger.besu.plugin.services.MetricsSystem;
import tech.pegasys.teku.infrastructure.async.OccurrenceCounter;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SambaTrackingExecutorFactory {

    private final OccurrenceCounter rejectedExecutionCounter;
    private final MetricsSystem metricsSystem;

    public SambaTrackingExecutorFactory(OccurrenceCounter rejectedExecutionCounter, MetricsSystem metricsSystem) {
        this.rejectedExecutionCounter = rejectedExecutionCounter;
        this.metricsSystem = metricsSystem;
    }


    public ExecutorService newCachedThreadPool(
            String name, int maxThreads, int maxQueueSize, ThreadFactory threadFactory) {
        ThreadPoolExecutor executor =
                new ThreadPoolExecutor(
                        maxThreads,
                        maxThreads,
                        60L,
                        TimeUnit.SECONDS,
                        new ArrayBlockingQueue(maxQueueSize),
                        threadFactory,
                        (r, e) -> onRejectedExecution(name));
        //TODO decide if we want to add it to MetricSystem.
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

    private void onRejectedExecution(String name) {
        this.rejectedExecutionCounter.increment();
        throw new RejectedExecutionException("Rejected execution on task queue - " + name);
    }
}
