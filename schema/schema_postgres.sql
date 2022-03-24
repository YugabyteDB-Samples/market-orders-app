CREATE SEQUENCE trade_id_seq CACHE 1000;
CREATE SEQUENCE buyer_id_seq CACHE 100;

CREATE TABLE Buyer(
    id integer NOT NULL DEFAULT nextval('buyer_id_seq'),
    first_name text NOT NULL,
    last_name text NOT NULL,
    age integer,
    goverment_id text,
    PRIMARY KEY(id)
);

CREATE TABLE Trade(
    id integer NOT NULL DEFAULT nextval('trade_id_seq'),
    buyer_id integer NOT NULL REFERENCES Buyer(id),
    symbol text,
    order_quantity integer,
    bid_price float,
    trade_type text,
    order_time timestamp(0) DEFAULT NOW(),
    PRIMARY KEY(id)
);
