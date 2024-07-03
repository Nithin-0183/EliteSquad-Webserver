CREATE DATABASE IF NOT EXISTS FormData;

USE FormData;

CREATE TABLE IF NOT EXISTS Messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sites (
    id INT PRIMARY KEY AUTO_INCREMENT,
    domain VARCHAR(255) NOT NULL,
    root VARCHAR(255) NOT NULL
);

INSERT INTO sites (domain, root) VALUES ('www.site1.com', '/var/www/site1');
INSERT INTO sites (domain, root) VALUES ('www.site2.com', '/var/www/site2');
