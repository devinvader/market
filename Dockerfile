# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY store/pom.xml store/
COPY payment/pom.xml payment/
COPY store/src store/src
COPY payment/src payment/src
RUN mvn clean package -DskipTests

# Runtime stage (Store, фронт крч)
FROM eclipse-temurin:21-jre-alpine AS store
WORKDIR /app
COPY --from=builder /app/store/target/store-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

# Runtime stage (Payment service)
FROM eclipse-temurin:21-jre-alpine AS payment
WORKDIR /app
COPY --from=builder /app/payment/target/payment-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]