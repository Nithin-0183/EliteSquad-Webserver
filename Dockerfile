FROM php:8.1-apache

# Enable Apache mods
RUN a2enmod rewrite ssl

# Copy custom Apache configuration
COPY apache_config.conf /etc/apache2/sites-available/000-default.conf

# Copy SSL certificates
COPY certfile.cer /etc/apache2/ssl/certfile.cer
COPY keystore.jks /etc/apache2/ssl/keystore.jks

# Set the working directory
WORKDIR /var/www

# Copy project files into the container
COPY WEB_ROOT /var/www

# Expose ports 80 and 443
EXPOSE 80 443

