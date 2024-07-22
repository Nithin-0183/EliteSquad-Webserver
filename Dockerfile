# Use an OpenJDK base image for Java 17
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the Java application JAR file into the container
COPY target/elitesquad-webserver-0.0.1-SNAPSHOT.jar /app/elitesquad-webserver-0.0.1-SNAPSHOT.jar

# Copy the JKS file into the container
COPY keystore.jks /etc/ssl/keystore.jks

# Copy the script to update /etc/hosts
COPY update-hosts.sh /app/update-hosts.sh
RUN chmod +x /app/update-hosts.sh

# Expose ports 80 and 443
EXPOSE 80 443

# Run the script and the Java application
# ENTRYPOINT ["/bin/sh", "-c", "/app/update-hosts.sh && java -jar /app/elitesquad-webserver-0.0.1-SNAPSHOT.jar"]

CMD ["tail", "-f", "/dev/null"]