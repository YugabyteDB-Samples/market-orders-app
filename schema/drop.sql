DROP MATERIALIZED VIEW IF EXISTS top_buyers_view;
DROP TABLE IF EXISTS Trade;
DROP TABLE IF EXISTS Buyer;
DROP SEQUENCE IF EXISTS buyer_id_seq CASCADE;
DROP SEQUENCE IF EXISTS trade_id_seq CASCADE;