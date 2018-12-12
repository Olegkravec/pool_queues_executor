package com.tpRunner;


public class ThreadCommand<DataType> implements Runnable {
    public DataType object;
    public ThreadCommand(DataType d) {
        this.object = d;
    }

    @Override
    public void run() {
    }
}
