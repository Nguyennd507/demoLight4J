DROP TABLE IF EXISTS orders;
CREATE TABLE  orders (
                        id bigint NOT NULL,
                        petId bigint NOT NULL,
                        quantity integer NOT NULL,
                        shipDate date NOT NULL ,
                        status varchar(50) NOT NULL,
                        PRIMARY KEY  (id)
);