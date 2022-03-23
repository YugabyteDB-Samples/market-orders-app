USE db;

CREATE TABLE Buyer(
    id integer NOT NULL AUTO_INCREMENT,
    first_name text NOT NULL,
    last_name text NOT NULL,
    age integer,
    goverment_id text,
    PRIMARY KEY(id)
);

CREATE TABLE Trade(
    id integer NOT NULL AUTO_INCREMENT,
    buyer_id integer NOT NULL REFERENCES Buyer(id),
    symbol text,
    order_quantity integer,
    bid_price float,
    trade_type text,
    order_time timestamp(0) DEFAULT NOW(),
    PRIMARY KEY(id)
);