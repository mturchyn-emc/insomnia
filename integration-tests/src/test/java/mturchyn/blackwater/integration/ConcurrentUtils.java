package mturchyn.blackwater.integration;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public final class ConcurrentUtils {


    private ConcurrentUtils() {}

    /**
     * Shut down executor service. If it has active tasks, then it waits for awaitTime
     * and then terminates active threads.
     *
     * @param executorService thread pool
     * @param secondsToWait time to wait in seconds
     */
    public static void shutDownExecutorService(ExecutorService executorService, long secondsToWait) {
        try {
            executorService.shutdown();
            if (!executorService.awaitTermination(secondsToWait, TimeUnit.SECONDS)) {
                List<Runnable> droppedTasks = executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

}
