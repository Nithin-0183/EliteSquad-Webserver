# Use image for PHP, Apache
FROM php:8.1-apache

# Copy PHP script
COPY ./WEB_ROOT /var/www/html

# Set Apache port
EXPOSE 80
