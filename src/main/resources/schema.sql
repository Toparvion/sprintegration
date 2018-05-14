CREATE TABLE TWEET
(
    id varchar2(24) PRIMARY KEY NOT NULL,
    author varchar2(64) NOT NULL,
    created date NOT NULL,
    tags varchar2(256),
    text varchar2(1024) NOT NULL
);
CREATE UNIQUE INDEX TWEET_id_uindex ON TWEET (id);