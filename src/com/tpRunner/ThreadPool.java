package com.tpRunner;

import java.util.concurrent.*;

public class ThreadPool {
    private final BlockingQueue queue = new ArrayBlockingQueue<Runnable>(10000);
    public ThreadPoolCallBacks callBacks;
    public ThreadPoolExecutor threadPool;
    public Integer AddedCommand = 0;
    public ThreadPool(int maxThreads, ThreadPoolCallBacks callBacks) {
        this.callBacks = callBacks;
        threadPool = new ThreadPoolExecutor(maxThreads, Integer.MAX_VALUE,
                0L, TimeUnit.MILLISECONDS, this.queue);

        threadPool.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                try {
                    executor.getQueue().put(r);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        threadPool.getActiveCount();
    }

    public boolean haveDoneAllTasks(){
        if(this.threadPool.getActiveCount() == 0 &&
                this.threadPool.getTaskCount() == this.threadPool.getCompletedTaskCount())
            return true;
        return false;

    }
}
