DROP TABLE IF EXISTS pet;
CREATE TABLE  pet (
                        id bigint NOT NULL,
                        name varchar(50) NOT NULL,
                        status varchar(50) NOT NULL,
                        amount bigint NOT NULL,
                        price bigint NOT NULL,
                        PRIMARY KEY  (id)
);