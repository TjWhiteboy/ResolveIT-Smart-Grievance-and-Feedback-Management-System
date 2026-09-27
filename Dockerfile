FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the generated JAR into the container
COPY target/*.jar app.jar

# We don't hardcode a port in the ENTRYPOINT, we let Spring Boot read the PORT env var
# but we expose a default port for documentation
EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]
