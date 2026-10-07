# =========================================================
# Restaurant API - Multi-stage Dockerfile
# =========================================================
# Steg 1 bygger Spring Boot-applikationen med Maven.
# Steg 2 kör endast den färdigbyggda JAR-filen.
# =========================================================

# ---------------------------------------------------------
# Steg 1 - Build
# ---------------------------------------------------------

# Maven + Java 25 används för att kompilera applikationen.
FROM maven:3.9.11-amazoncorretto-25 AS build

# Skapa och sätt arbetsmapp
WORKDIR /app

# Copy pom.xml first — Docker caches this layer.
# Maven dependencies are only re-downloaded when pom.xml changes,
# NOT on every source code change. This speeds up repeated builds.
COPY pom.xml .
RUN mvn dependency:go-offline --batch-mode --no-transfer-progress

# Kopiera över källkoden till arbetsmappen
COPY src ./src
# Bygg applikationen med Maven
RUN mvn clean package -DskipTests --batch-mode --no-transfer-progress

# ---------------------------------------------------------
# Steg 2 - Runtime
# ---------------------------------------------------------

# Den färdiga applikationen behöver inte Maven.
# Endast Java 25 behövs för att köra JAR-filen.
FROM amazoncorretto:25-alpine

# Skapa och sätt arbetsmapp
WORKDIR /app

# Installera curl för healthcheck. Utcommenterat eftersom railway kan kontrollera
# ditt /health-endpoint utan att vi behöver installera ett extra program inne i containern

# RUN apk add --no-cache curl

# Kopiera över applikationen från steg 1 "build" och döp den till app.jar
COPY --from=build /app/target/*.jar app.jar

# Spring Boot kör normalt på port 8080.
EXPOSE 8080

# Starta Restaurant API
CMD ["java", "-jar", "app.jar"]
