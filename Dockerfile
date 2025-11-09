# Multi-stage build for Java application
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Install FFmpeg for transcoding
RUN apk add --no-cache ffmpeg nginx

WORKDIR /app

# Copy built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Create directories for streaming
RUN mkdir -p /var/streaming /var/recordings /var/lstream

# Expose ports
# 8080 - Application
# 1935 - RTMP
# 80 - HLS/HTTP
EXPOSE 8080 1935 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
