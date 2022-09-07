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

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Scanner;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class Application {
    /* Market orders stream. */
    private static MarketOrdersStream ordersStream;

    private static String loadScript;

    public static void main(String args[]) {
        int tradeStatsInterval = 0;
        String connectionPropsFile = "./properties/postgres.properties";
        boolean refreshMaterializedView = true;

        if (args != null) {
            for (String arg : args) {
                if (arg.startsWith("tradeStatsInterval")) {
                    tradeStatsInterval = Integer.parseInt(arg.split("=")[1].trim());

                } else if (arg.startsWith("connectionProps")) {
                    connectionPropsFile = arg.split("=")[1].trim();
                } else if (arg.startsWith("loadScript")) {
                    loadScript = arg.split("=")[1].trim();
                } else if (arg.startsWith("refreshView")) {
                    // workaround for read replicas case:
                    // https://yugabyte.atlassian.net/browse/DB-268
                    refreshMaterializedView = Boolean.valueOf(arg.split("=")[1].trim());
                }
            }
        }

        Application app = new Application();

        try {
            System.out.println("Connecting to the database...");

            HikariDataSource dataSource = app.openDataSource(connectionPropsFile);

            System.out.println("Connected to the database: " + dataSource.getJdbcUrl());

            if (loadScript != null) {
                System.out.println("Performing initial data load...");
                app.loadData(dataSource);
                System.out.println("Loaded schema and data");
            }

            System.out.println("Connecting to the market orders stream...");
            ordersStream = new MarketOrdersStream(dataSource, refreshMaterializedView);
            ordersStream.start();
            System.out.println("Connected to the stream");

            if (tradeStatsInterval > 0) {
                TradeStats stats = new TradeStats(dataSource, tradeStatsInterval);
                stats.printStats();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HikariDataSource openDataSource(String connPropsFile) throws IOException {
        Properties connProps = new Properties();

        connProps.load(new FileInputStream(connPropsFile));

        HikariConfig config = new HikariConfig(connProps);
        config.validate();

        return new HikariDataSource(config);
    }

    private void loadData(HikariDataSource dataSource) {
        try {
            Scanner scanner = new Scanner(new FileInputStream(loadScript));
            scanner.useDelimiter(";");

            Connection conn = dataSource.getConnection();
            Statement statement = conn.createStatement();

            while (scanner.hasNext()) {
                String nextCommand = scanner.next().trim();
                System.out.println(nextCommand);
                statement.execute(nextCommand);
            }

            conn.close();
        } catch (Exception ex) {
            System.err.println("Failed database preloading");
            ex.printStackTrace();
        }
    }
}
