DROP TABLE IF EXISTS users;
CREATE TABLE  users (
                        id bigint NOT NULL,
                        userName varchar(50) NOT NULL,
                        firstName varchar(50) NOT NULL ,
                        lastName varchar(50) NOT NULL,
                        password varchar(100) NOT NUll,
                        phone varchar(20) NOT NUll,
                        status varchar(50) NOT NULL,
                        PRIMARY KEY  (id)
);