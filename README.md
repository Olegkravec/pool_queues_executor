# Pool Queues Executor

## Base usage
Defining:
```aidl
    private static ThreadPoolsRunner<Integer> PoolsExecutor = new ThreadPoolsRunner<>(RUN_POLICY.RUN_EXECUTOR, THREAD_POLICY.TOTAL_LIMIT, 1000, 10);
```

Adding new pool with callback
```aidl
PoolsExecutor.newPool(1, () -> System.out.println("My custom pool done!"));
```

Adding new command to queue
```aidl
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
```

Run all pools
```aidl
PoolsExecutor.executeAll();
```

## Description
soon...