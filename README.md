# Market Orders Data Stream Processing With YugabyteDB

Storing and processing real-time data stream with YugabyteDB.

## Build and run Java app

* Build and package the app:
    ```shell
    mvn clean package 
    ```
* Run the app:
```shell
java -jar target/market-orders-app.jar
```    

## Prepare Arctype Queries and Dashboards

Add the following queries to Archtype and execute them periodically:

* Max trade ID number (to confirm the data is being added to the database):
    ```sql
    select max(id) from trade;
    ```
* Most popular symbols:
    ```sql
    SELECT symbol, SUM(bid_price) as total FROM Trade GROUP BY(symbol) ORDER BY total DESC;
    ``` 

* Top buyers:
    ```sql
    SELECT buyer.id, first_name, last_name, sum(bid_price) as total FROM Buyer
    JOIN Trade ON trade.buyer_id = buyer.id
    GROUP BY (buyer.id) ORDER BY total DESC;
    ```

## Demo Case 1: Live Infrastructure Update

1. Connect the app to Yugabyte Cloud
2. Execute SQL queries from the Archtype section above to confirm the cluster is under load.
3. Use the infrastructure upgrade feature of the cloud.
4. While the infrastructure is being upgraded, show that the same queries keep executing. 
    * Note, an INSERT can fail in the app saying `The admin is shutting down or restarting a node the connection was open with.`. 
    This operation will be repetead again using another connection from the pool.
    * A SELECT might fail in Arctype due to the same reason. Arctype will use another connection next time you ask to execute the query.

## Demo Case 2: Zone-Level Resiliency

1. Connect the app to a Yugabyte Platform cluster.
