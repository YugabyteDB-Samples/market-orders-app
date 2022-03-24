package com.yugabyte.app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
                        Connection conn = dataSource.getConnection();
                        
                        PreparedStatement pStatement = conn.prepareStatement(
                            "select max(id) from Trade");
                        
                        ResultSet result = pStatement.executeQuery();
                        result.next();

                        System.out.println("============= Trade Stats ============\n");
                        System.out.println("Trades Count: " + result.getInt(1) + "\n");
                        
                        System.out.format("%-16s%-16s\n", "Stock", "Total Proceeds");
                        
                        pStatement = conn.prepareStatement(
                            "SELECT symbol, SUM(bid_price) as total FROM Trade GROUP BY(symbol) ORDER BY total DESC");
                        result = pStatement.executeQuery();
                        
                        while (result.next()) {
                            System.out.format("%-16s%-16f\n", result.getString(1), result.getFloat(2));
                        }

                        System.out.println("======================================\n\n");

                        // returning connection to the pool (it's not being closed)
                        conn.close();
                    } catch (SQLException e) {   
                        if (e.getSQLState().equals("57P01")) {
                            System.err.println(e.getMessage());
                            System.out.println("Will execute the rejected query again...");
                        } else {
                            e.printStackTrace();
                            return;
                        }   
                    }

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
