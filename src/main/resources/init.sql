CREATE DATABASE IF NOT EXISTS WebServerDB;

USE WebServerDB;

-- Table to store information about users who upload the applications
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL
);

-- Table to store the status types with predefined IDs
CREATE TABLE IF NOT EXISTS statuses (
    id INT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

-- Table to store information about the hosted sites
CREATE TABLE IF NOT EXISTS sites (
    id INT PRIMARY KEY AUTO_INCREMENT,
    domain VARCHAR(255) NOT NULL,
    root VARCHAR(255) NOT NULL,
    user_id INT,
    upload_path VARCHAR(255),
    ip_address VARCHAR(45) NOT NULL,  -- To store IPv4 or IPv6 addresses
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status_id INT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (status_id) REFERENCES statuses(id)
);

-- Insert predefined status data into statuses table
INSERT INTO statuses (id, name) VALUES 
(12100, 'Running'), 
(12101, 'Off'), 
(12102, 'Error');

-- Insert sample data into users table
INSERT INTO users (username, email) VALUES ('admin', 'admin@ucd.ie');

-- Insert sample data into sites table
INSERT INTO sites (domain, root, user_id, upload_path, ip_address, status_id) VALUES 
('site1.local', '/app/WEB_ROOT/site1', 1, '/uploads/site1.zip', '127.0.0.2', 12100),
('site2.local', '/app/WEB_ROOT/site2', 1, '/uploads/site2.zip', '127.0.0.2', 12100);
