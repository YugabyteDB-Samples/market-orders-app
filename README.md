# Market Orders Data Stream Processing With YugabyteDB

Storing and processing real-time data stream with YugabyteDB.

## Start MySQL in Docker

* Start a MySQL instance and load sample data:
    ```shell
    docker-compose -f docker-compose-mysql.yml up
    ```

    The container exposes port `3307` for connections from the host.

## Migration from Postgres to Yugabyte Cloud

1. Navigate to the root of the project.

2. Migrate the schema and data from Postgres:
    ```shell
    yb_migrate export --source-db-type postgresql --source-db-uri postgresql://postgres:postgres@localhost:5438  --export-dir ./migrations 
    ```
3. Import schema to your Yugabyte Cloud cluster:
    ```shell
    yb_migrate import schema --target-db-host c7d3829a-0fb4-493e-8684-60e75739bf6b.aws.ybdb.io --target-db-port 5433 --target-db-user admin --target-db-password t0T-BdVCu2opsgoWsG5h-nZqMdakfV --target-db-name yugabyte --export-dir ./migrations
    ```
4. Import data to Yugabyte Cloud:
    ```shell
    yb_migrate import data --target-db-host c7d3829a-0fb4-493e-8684-60e75739bf6b.aws.ybdb.io --target-db-port 5433 --target-db-user admin --target-db-password t0T-BdVCu2opsgoWsG5h-nZqMdakfV --target-db-name yugabyte --target-ssl-mode verify-full --target-ssl-root-cert /Users/dmagda/Downloads/root.crt --export-dir ./migrations --start-clean YES
    ```    

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
    select max(id) from Trade;
    ```
* Most popular symbols:
    ```sql
    SELECT symbol, SUM(bid_price) as total FROM Trade GROUP BY(symbol) ORDER BY total DESC;
    ``` 

* Top buyers:
    ```sql
    SELECT Buyer.id, first_name, last_name, sum(bid_price) as total FROM Buyer
    JOIN Trade ON Trade.buyer_id = Buyer.id
    GROUP BY (Buyer.id) ORDER BY total DESC;
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

1. Deploy a multi-zone three node cluster with Yugabyte Platform.
2. Connect the app to the Platform cluster. Make sure to provide the `dataSource.additionalEndpoints` parameter in the `properties\yugabyte-template.properties` file.
3. Show that INSERTs from the app and SELECTS on the Archtype end succeeed.
4. Kill one of the nodes (not the one Archtype is connected to).

