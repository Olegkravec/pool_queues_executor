package com.tpRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class ThreadPoolsRunner <queueType>{
    private Semaphore QueuePoolMutex = new Semaphore(2);
    private HashMap<queueType, ThreadPool> queue_pool;
    private ThreadCommand<HashMap<queueType, ThreadPool>> monitorThread;
    private RUN_POLICY run_policy = RUN_POLICY.RUN_EXECUTOR;
    private THREAD_POLICY thread_policy = THREAD_POLICY.TOTAL_LIMIT;
    private int maxThreads = 9000;
    private long recheckTime = 300;
    private int minimumCommands = 10;
    public long runnedCommands = 0;
    public long runnedPools = 0;
    public ThreadPoolsRunner(RUN_POLICY run_policy, THREAD_POLICY thread_policy, int maxThreads, long MonitorRecheckTime) {
        this.run_policy = run_policy;
        this.thread_policy = thread_policy;
        this.maxThreads = maxThreads;
        this.recheckTime = MonitorRecheckTime;
        this.queue_pool = new HashMap<>();

        this.runMonitor();
    }

    private void runMonitor(){
        this.monitorThread = new ThreadCommand<HashMap<queueType, ThreadPool>>(null) {
            @Override
            public void run() {

                while (true){
                    runnedCommands = 0;
                    runnedPools = queue_pool.size();
                    try { QueuePoolMutex.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        continue;
                    }
                    HashMap<queueType, ThreadPool> TempBufferOfQueuePoll = new HashMap<queueType, ThreadPool>(queue_pool);
                    QueuePoolMutex.release();

                    for(Map.Entry<queueType, ThreadPool> entry : TempBufferOfQueuePoll.entrySet()) {
                        queueType queueID = entry.getKey();
                        ThreadPool queuePool = entry.getValue();

                        runnedCommands += queuePool.threadPool.getPoolSize();
                        //System.out.println("QUEUE ID: " +queueID+ " HAS " + queuePool.haveDoneAllTasks());
                        if(queuePool.haveDoneAllTasks()){
                            queuePool.callBacks.OnPoolDone();
                            if(queuePool.AddedCommand > 0) poolRemove(queueID);
                        }else{
                            System.out.format("\nQueueID: %d\tDone:%d\tActive:%d\tSizeS:%d\tSizeP:%d", queueID, queuePool.threadPool.getCompletedTaskCount(), queuePool.threadPool.getActiveCount(), queuePool.threadPool.getPoolSize(),queuePool.threadPool.getTaskCount() );
                        }

                        if(thread_policy == THREAD_POLICY.POOL_LIMIT){
                            queuePool.threadPool.setCorePoolSize(maxThreads);
                        }else{
                            queuePool.threadPool.setCorePoolSize(calculateLimits());
                        }

                    }

                    //System.out.println("YUO HAVE " + runnedCommands + " RUNNED POOLS AND " + runnedCommands + " RUNNED COMMANDS");
                    try { Thread.sleep(1000);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        new Thread(this.monitorThread).start();
    }

    private void poolRemove(queueType T){
        try { QueuePoolMutex.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        queue_pool.remove(T);
        QueuePoolMutex.release();
    }

    public boolean newPool(queueType poolID, ThreadPoolCallBacks callBacks){
        if(queue_pool.containsKey(poolID))
            return false;

        try { QueuePoolMutex.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        queue_pool.put(poolID, new ThreadPool(this.maxThreads, callBacks));
        QueuePoolMutex.release();
        if (run_policy.equals(RUN_POLICY.RUN_PRESTART)) {
            if (this.thread_policy == THREAD_POLICY.POOL_LIMIT) {
                (queue_pool.get(poolID)).threadPool.setCorePoolSize(this.maxThreads);
            } else {
                (queue_pool.get(poolID)).threadPool.setCorePoolSize(this.calculateLimits());
            }
            (queue_pool.get(poolID)).threadPool.prestartAllCoreThreads();
        }
        return true;
    }

    public boolean poolAddThread(queueType t, ThreadCommand c){
        try {
            (queue_pool.get(t)).threadPool.getQueue().put(c);
            (queue_pool.get(t)).AddedCommand++;
            if(this.run_policy == RUN_POLICY.RUN_ONADD){
                (queue_pool.get(t)).threadPool.prestartAllCoreThreads();
            }
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean poolExecute(queueType t){
        if(!queue_pool.containsKey(t)) return false;
        if(this.thread_policy == THREAD_POLICY.POOL_LIMIT){
            (queue_pool.get(t)).threadPool.setCorePoolSize(this.maxThreads);
        }else{
            (queue_pool.get(t)).threadPool.setCorePoolSize(this.calculateLimits());
        }
        (queue_pool.get(t)).threadPool.prestartAllCoreThreads();
        return true;
    }

    public void executeAll(){
        try { QueuePoolMutex.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        HashMap<queueType, ThreadPool> TempBufferOfQueuePoll = new HashMap<queueType, ThreadPool>(queue_pool);
        QueuePoolMutex.release();
        for(Map.Entry<queueType, ThreadPool> entry : TempBufferOfQueuePoll.entrySet()) {
            ThreadPool pool = entry.getValue();
            if(this.thread_policy == THREAD_POLICY.POOL_LIMIT){
                pool.threadPool.setCorePoolSize(this.maxThreads);
            }else{
                pool.threadPool.setCorePoolSize(this.calculateLimits());
            }
            pool.threadPool.prestartAllCoreThreads();
        }
    }

    public int calculateLimits(){
        try { QueuePoolMutex.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        HashMap<queueType, ThreadPool> TempBufferOfQueuePoll = new HashMap<queueType, ThreadPool>(queue_pool);
        QueuePoolMutex.release();

        int runned = 1;
        for(Map.Entry<queueType, ThreadPool> entry : TempBufferOfQueuePoll.entrySet()) {
            ThreadPool pool = entry.getValue();
            runned+=pool.threadPool.getQueue().size();
        }
        int calced = (runned / (queue_pool.size()+1)) + this.minimumCommands;
        if(calced >= this.maxThreads) return this.maxThreads;
            else return calced;
    }

    public long getPollTaskCount(queueType T){
        if(!queue_pool.containsKey(T)) return -1;
        return (queue_pool.get(T)).threadPool.getTaskCount();
    }
    public long getPoolCompletedTaskCount(queueType T){
        if(!queue_pool.containsKey(T)) return -1;
        return (queue_pool.get(T)).threadPool.getCompletedTaskCount();
    }
    public int getPollActiveTaskCount(queueType T){
        if(!queue_pool.containsKey(T)) return -1;
        return (queue_pool.get(T)).threadPool.getActiveCount();
    }

    public int QueueSize(){
        return queue_pool.size();
    }

    public int PoolSize(queueType T){
        if(!queue_pool.containsKey(T)) return -1;
        return (queue_pool.get(T)).threadPool.getQueue().size();
    }
}
