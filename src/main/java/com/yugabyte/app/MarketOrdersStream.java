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
    private String STREAM_SUBSCRIPION_KEY = "sub-c-4377ab04-f100-11e3-bffd-02ee2ddab7fe";

    /**
     * A pool of connections to the database.
     */
    private HikariDataSource dataSource;

    private int buyersCount;

    public MarketOrdersStream(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void start() throws SQLException {
        buyersCount = dataSource.getConnection().createStatement().
            executeQuery("SELECT max(id) FROM Buyer").getInt(0);

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

    private class StreamCallback extends SubscribeCallback {

        /**
         * @param nub
         * @param status
         */
        public void status(PubNub nub, PNStatus status) {

            if (status.getCategory() == PNStatusCategory.PNConnectedCategory) {
                // Connect event.
                System.out.println("Connected to the market orders stream: " + status.toString());
            }
            else if (status.getCategory() == PNStatusCategory.PNUnexpectedDisconnectCategory) {
                System.err.println("Connection is lost:" + status.getErrorData().toString());
            }
            else if (status.getCategory() == PNStatusCategory.PNReconnectedCategory) {
                // Happens as part of our regular operation. This event happens when
                // radio / connectivity is lost, then regained.
                System.out.println("Reconnected to the market orders stream");
            }
            else {
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

            try {
                Connection conn = dataSource.getConnection();
                PreparedStatement pStatement = conn.prepareStatement(
                    "INSERT INTO Trade (buyer_id, symbol, order_quantity, bid_price, trade_type) VALUES(?,?,?,?,?)");

                pStatement.setInt(1, new Random().nextInt(buyersCount) + 1);
                pStatement.setString(2, json.get("symbol").getAsString());
                pStatement.setInt(3, json.get("order_quantity").getAsInt());
                pStatement.setDouble(4, json.get("bid_price").getAsDouble());
                pStatement.setString(5, json.get("trade_type").getAsString());

                pStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
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
