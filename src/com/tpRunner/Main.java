package com.tpRunner;

import java.util.Random;

public class Main {
    private static ThreadPoolsRunner<Integer> PoolsExecutor = new ThreadPoolsRunner<>(RUN_POLICY.RUN_EXECUTOR, THREAD_POLICY.TOTAL_LIMIT, 1000, 10);
    public static void main(String[] args) throws InterruptedException {
        PoolsExecutor.newPool(1, () -> System.out.println("My custom pool done!"));
        for (int u = 1; u < 10000; u++){


            PoolsExecutor.poolAddThread(1, new ThreadCommand<String>(" for pool:" + u) {
                @Override
                public void run() {
                    Random rand = new Random();
                    int random = rand.nextInt(10) + 1;
                    System.out.println("Running thread num: " + object + "\t"+Thread.currentThread().getId());
                    try {
                        Thread.sleep(random);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            PoolsExecutor.executeAll();




            System.out.println("Queues size: " + PoolsExecutor.QueueSize());
            System.out.println("Queue size of 1: " + PoolsExecutor.PoolSize(u));
            System.out.println("Queue active for 1: " + PoolsExecutor.getPollActiveTaskCount(u));
            System.out.println("Queue tasks for 1: " + PoolsExecutor.getPollTaskCount(u));
            System.out.println("Queue complated tasks for 1: " + PoolsExecutor.getPoolCompletedTaskCount(u));
        }
        Thread.sleep(3000);
        System.out.println("SPEED OF POOLS MUST BE: " + PoolsExecutor.calculateLimits());

    /*
        threadPool.prestartAllCoreThreads();
            threadPool.getQueue().put(new ThreadCommand<Integer>(i){
                @Override
                public void run() {
                    System.out.println("Running thread num: " + object);
                }
            });
        }*/
    }
}
