CREATE TABLE Writers (
    id INTEGER NOT NULL PRIMARY KEY,
    first_name    VARCHAR(15) NOT NULL,
    middle_name    VARCHAR(15),
    last_name    VARCHAR(15) NOT NULL,
    birth_date    VARCHAR(10) NOT NULL,
    death_date    VARCHAR(10),
    country_of_origin    VARCHAR(20) NOT NULL
);