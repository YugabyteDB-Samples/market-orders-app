/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
                            System.out.println("Retrying the rejected query...");

                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                            continue;
                        } else {
                            e.printStackTrace();
                            continue;
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
