# Market Orders Streaming to YugabyteDB

The application subscribes to the [PubNub's market orders stream](https://www.pubnub.com/developers/realtime-data-streams/financial-securities-market-orders/) and stores the market trades in an underlying database. You can switch between MySQL, PostgreSQL and YugabyteDB with no need to update the application logic.

## Start Database
 
* Start a MySQL instance and load sample data:
    ```shell
    docker-compose -f docker-compose-mysql.yml up
    ```

    Use port `3307` for connections from the host machine.

* or start a PostgreSQL database instead:
    ```shell
    docker-compose -f docker-compose-postgres.yml up
    ```

    Use port `5438` for connections from the host machine.

* or start a [YugabyteDB Managed](https://docs.yugabyte.com/latest/yugabyte-cloud/cloud-quickstart/) instance, and load the schema (`./schema/schema_postgres.sql`) and sample data (`./schema/data.sql`).

## Build and run Java app

* Build and package the app:
    ```shell
    mvn clean package 
    ```
* Run the app by connecting to the selected database:

    * Connect to Postgres (`./properties/postgres.properties`):
    ```shell
    java -jar target/market-orders-app.jar connectionProps=./properties/postgres.properties loadScript=./schema/schema_postgres.sql tradeStatsInterval=5000
    ```
    * Connect to MySQL (`./properties/mysql.properties`):
    ```shell
    java -jar target/market-orders-app.jar connectionProps=./properties/mysql.properties loadScript=./schema/schema_mysql.sql tradeStatsInterval=5000
    ``` 
    * Connect to YugabyteDB Managed after providing connecting settings in the `./properties/yugabyte-template.properties` file:
    ```shell
    java -jar target/market-orders-app.jar connectionProps=./properties/yugabyte-template.properties loadScript=./schema/schema_postgres.sql tradeStatsInterval=5000
    ```    

The `tradeStatsInterval` (measured in milliseconds) instructs the `TradeStats.java` service to query trade-related statistics from the database within the specified interval. If the interval is <= `0` or not set, then the statistics will not be collected.

## Advanced Demo 

The app can be used to demonstrate the migration from PostreSQL/MySQL to YugabyteDB as well as high-avalability and scalability capabilities of YugabyteDB. 
Follow [this page](./demo/demo_sript.md) for more details.

## Run in Docker

1. Build the app:
    ```shell
    mvn clean package 
    ```
2. Create an image:
    ```shell
    docker rmi market-orders-app
    docker build -t market-orders-app .
    ```

3. Start the app inside a container:
    ```shell
    docker run --name market-orders-instance --net custom-network \
    market-orders-app:latest \
    java -jar /home/target/market-orders-app.jar \
    connectionProps=/home/yugabyte-docker.properties \
    loadScript=/home/schema_postgres.sql \
    tradeStatsInterval=5000
    ```