FROM php:8.1-apache

# Enable Apache mods
RUN a2enmod rewrite

# Copy custom Apache configuration
COPY apache_config.conf /etc/apache2/sites-available/000-default.conf

# Set the working directory
WORKDIR /var/www

# Copy project files into the container
COPY WEB_ROOT /var/www

# Expose port 80
EXPOSE 80
