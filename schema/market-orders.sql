CREATE SEQUENCE trade_id_seq CACHE 1000;
CREATE SEQUENCE buyer_id_seq CACHE 100;

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

CREATE TABLE Buyer(
    id integer NOT NULL DEFAULT nextval('buyer_id_seq'),
    first_name text NOT NULL,
    last_name text NOT NULL,
    age integer,
    goverment_id text,
    PRIMARY KEY(id)
);

INSERT INTO Buyer(first_name, last_name,age, goverment_id) VALUES
('John', 'Smith', 45, '7bfjd73'),
('Arnold', 'Mazer', 55, 'unb23212'),
('Lara', 'Croft', 35, '12338fb31'),
('Patrick', 'Green', 42, 'asbn233'),
('Anna', 'Romanoff', 46, 'klnnk3823'),
('Alfred', 'Black', 55, '32345'),
('San', 'Newman', 28, 'fjdks28943kd'),
('Henry', 'McDonald', 31, 'dasdnouqbwe'),
('Liza', 'Connor', 33, '1823bjkffe923');