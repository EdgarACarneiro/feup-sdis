package ThreadPool;

import Action.Action;
import Channel.MulticastChannel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class implementing a Thread Pool
 */
public class ThreadPool {

    /**
     * The number of threads in the pool
     */
    private static final int THREAD_POOL_SIZE = 10;

    /**
     * Executor for executing the threads
     */
    private ExecutorService executor;

    /**
     * ThreadPool constructor
     */
    public ThreadPool() {
        executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    /**
     * Run a new thread with a given action
     *
     * @param action action to be executed
     */
    public void executeThread(Action action) {
        executor.execute(action);
    }

    /**
     * Run a new thread with a given channel, initiating its listening functionality
     *
     * @param channel permanent listener channel
     */
    public void executeThread(MulticastChannel channel) {
        executor.execute(channel);
    }

    /**
     * Terminate the ThreadPool execution.
     * Waits for the threads to terminate.
     */
    public void shutDown() {
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }
}
