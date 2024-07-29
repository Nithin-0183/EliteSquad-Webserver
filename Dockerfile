# Use an OpenJDK base image for Java 20
FROM openjdk:20-jdk-slim

# Set the working directory
WORKDIR /app

# Install necessary packages including PHP and php-cgi
RUN apt-get update && \
    apt-get install -y \
    php \
    php-cgi \
    build-essential \
    curl && \
    apt-get clean

# Copy the Java application JAR file into the container
COPY target/elitesquad-webserver-0.0.1-SNAPSHOT.jar /app/elitesquad-webserver-0.0.1-SNAPSHOT.jar

# Copy the application.properties file into the container
COPY src/main/resources/application.properties /app/application.properties

# Copy the JKS file into the container
COPY keystore.jks /etc/ssl/keystore.jks

# Copy the WEB_ROOT directory into the container
COPY WEB_ROOT /app/WEB_ROOT/

# Copy the scripts into the container
COPY update-hosts.sh /app/update-hosts.sh
COPY modify_hosts.sh /app/modify_hosts.sh
COPY wrk_test.sh /app/wrk_test.sh

# Copy the Maven build files and project files
COPY pom.xml /app/pom.xml
COPY src /app/src

# Make the scripts executable
RUN chmod +x /app/update-hosts.sh /app/modify_hosts.sh /app/wrk_test.sh

# Expose ports 8080 and 8443
EXPOSE 8080 8443

# Run the script and the Java application
ENTRYPOINT ["sh", "-c", "/app/update-hosts.sh && exec java -jar /app/elitesquad-webserver-0.0.1-SNAPSHOT.jar"]

# Keep the container running
CMD ["tail", "-f", "/dev/null"]
