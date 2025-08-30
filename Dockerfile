FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy pom.xml first for better layer caching
COPY pom.xml .

# Install Maven and download dependencies
RUN apt-get update && apt-get install -y maven && \
    mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests including compilation)
RUN mvn clean package -Dmaven.test.skip=true && \
    apt-get remove -y maven && \
    apt-get autoremove -y && \
    rm -rf /var/lib/apt/lists/*

EXPOSE 8080

# Run with development profile to ensure proper template handling
CMD ["java", "-jar", "-Dspring.profiles.active=default", "-Dspring.thymeleaf.cache=false", "target/chess-club-admin-1.0.0.jar"]