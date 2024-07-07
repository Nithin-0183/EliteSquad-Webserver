CREATE DATABASE IF NOT EXISTS FormData;

USE FormData;

CREATE TABLE IF NOT EXISTS sites (
    id INT PRIMARY KEY AUTO_INCREMENT,
    domain VARCHAR(255) NOT NULL,
    root VARCHAR(255) NOT NULL
);

INSERT INTO sites (domain, root) VALUES ('site1.local', '/WEB_ROOT/site1');
INSERT INTO sites (domain, root) VALUES ('site2.local', '/WEB_ROOT/site2');
