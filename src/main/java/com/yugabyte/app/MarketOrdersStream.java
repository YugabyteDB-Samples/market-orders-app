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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Random;

public class MarketOrdersStream {
    /**
     * Stream object.
     */
    private PubNub stream;

    /**
     * PubNub stream name.
     */
    protected final static String STREAM_NAME = "pubnub-market-orders";

    /**
     * PubNub stream subscription key.
     */
    private String STREAM_SUBSCRIPION_KEY = "sub-c-99084bc5-1844-4e1c-82ca-a01b18166ca8";

    /**
     * A pool of connections to the database.
     */
    private HikariDataSource dataSource;

    private int buyersCount;

    private long writeLatency;

    public MarketOrdersStream(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void start() throws SQLException {
        ResultSet result = dataSource.getConnection().createStatement().executeQuery("SELECT max(id) FROM Buyer");
        result.next();
        buyersCount = result.getInt(1);

        PNConfiguration cfg = new PNConfiguration();
        cfg.setSubscribeKey(STREAM_SUBSCRIPION_KEY);

        stream = new PubNub(cfg);

        stream.addListener(new StreamCallback());
        stream.subscribe().channels(Arrays.asList(STREAM_NAME)).execute();
    }

    public void stop() {
        stream.unsubscribe().execute();
        dataSource.close();
    }

    public long getWriteLatency() {
        return writeLatency;
    }

    public int getBuyersCount() {
        return buyersCount;
    }

    private class StreamCallback extends SubscribeCallback {

        /**
         * @param nub
         * @param status
         */
        public void status(PubNub nub, PNStatus status) {

            if (status.getCategory() == PNStatusCategory.PNConnectedCategory) {
                // Connect event.
                System.out.println("Connected to the market orders stream: " + status.toString());
            } else if (status.getCategory() == PNStatusCategory.PNUnexpectedDisconnectCategory) {
                System.err.println("Connection is lost:" + status.getErrorData().toString());
            } else if (status.getCategory() == PNStatusCategory.PNReconnectedCategory) {
                // Happens as part of our regular operation. This event happens when
                // radio / connectivity is lost, then regained.
                System.out.println("Reconnected to the market orders stream");
            } else {
                System.out.println("Connection status changes:" + status.toString());
            }
        }

        /**
         * @param nub
         * @param result
         */
        public void message(PubNub nub, PNMessageResult result) {
            JsonElement mes = result.getMessage();
            JsonObject json = mes.getAsJsonObject();

            while (true) {
                try {
                    Connection conn = dataSource.getConnection();

                    long start = System.currentTimeMillis();

                    PreparedStatement pStatement = conn.prepareStatement(
                            "INSERT INTO Trade (buyer_id, symbol, order_quantity, bid_price, trade_type) VALUES(?,?,?,?,?)");

                    pStatement.setInt(1, new Random().nextInt(buyersCount) + 1);
                    pStatement.setString(2, json.get("symbol").getAsString());
                    pStatement.setInt(3, json.get("order_quantity").getAsInt());
                    pStatement.setDouble(4, json.get("bid_price").getAsFloat());
                    pStatement.setString(5, json.get("trade_type").getAsString());

                    pStatement.executeUpdate();

                    writeLatency = System.currentTimeMillis() - start;
                    // returning connection to the pool (it's not being closed)
                    conn.close();

                    return;
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
            }
        }

        /**
         * @param nub
         * @param result
         */
        public void presence(PubNub nub, PNPresenceEventResult result) {
            System.out.println("Stream presence event: " + result.toString());
        }
    }

}
