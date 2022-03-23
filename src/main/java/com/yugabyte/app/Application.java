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
            HikariDataSource dataSource = app.openDataSource();

            // Starting Market Ticker.
            ordersStream = new MarketOrdersStream(dataSource);
            ordersStream.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HikariDataSource openDataSource() throws IOException {
        Properties connProps = new Properties();
        
        connProps.load(new FileInputStream(
            "/Users/dmagda/Downloads/sample_projects/market-orders-app/properties/yugabyte.properties"));
        
        HikariConfig config = new HikariConfig(connProps);
        config.validate();

        return new HikariDataSource(config);
    }
}
