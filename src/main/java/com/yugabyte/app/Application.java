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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class Application {
    /* Market orders stream. */
    private static MarketOrdersStream ordersStream;

    public static void main(String args[]) {
        // int execTime = DEFAULT_EXEC_TIME_MINS;

        // if (args != null) {
        //     for (String arg : args) {
        //         if (arg.startsWith("execTime")) {
        //             execTime = Integer.parseInt(arg.split("=")[1]);
        //         } else {
        //             System.err.println("Unsupported parameter: " + execTime);
        //             return;
        //         }
        //     }
        // }

        Application app = new Application();

        try {
            System.out.println("Connecting to the database...");

            HikariDataSource dataSource = app.openDataSource();

            System.out.println("Connected to the database: " + dataSource.getJdbcUrl());

            System.out.println("Connecting to the market orders stream...");
            ordersStream = new MarketOrdersStream(dataSource);
            ordersStream.start();
            System.out.println("Connected to the stream");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HikariDataSource openDataSource() throws IOException {
        Properties connProps = new Properties();
        
        connProps.load(new FileInputStream(
            "/Users/dmagda/Downloads/sample_projects/market-orders-app/properties/yugabyte-platform-example.properties"));
        
        HikariConfig config = new HikariConfig(connProps);
        config.validate();

        return new HikariDataSource(config);
    }
}
