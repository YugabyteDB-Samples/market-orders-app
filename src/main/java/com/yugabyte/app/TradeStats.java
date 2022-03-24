package com.yugabyte.app;

import com.zaxxer.hikari.HikariDataSource;

public class TradeStats {
    /**
     * A pool of connections to the database.
     */
    private HikariDataSource dataSource;

    private int statsInterval;

    private volatile boolean keepWorking;

    public TradeStats(HikariDataSource dataSource, int statsInterval) {
        this.dataSource = dataSource;
        this.statsInterval = statsInterval;
    }    

    public void printStats() {
        keepWorking = true;

        new Thread(new Runnable() {

            @Override
            public void run() {
                while (keepWorking) {
                    

                    try {
                        Thread.sleep(statsInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                
            }
        }).start();
    }

    public void stop() {
        keepWorking = false;
    }


}
