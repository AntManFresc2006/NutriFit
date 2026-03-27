# ── Etapa 1: compilación ────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copiar solo los pom.xml primero para aprovechar la caché de capas de Maven
COPY pom.xml .
COPY backend/pom.xml backend/pom.xml
COPY client/pom.xml client/pom.xml

# Descargar dependencias del módulo backend (cacheado mientras los pom no cambien)
RUN mvn -pl backend -am dependency:go-offline -q

# Copiar el código fuente del backend y compilar
COPY backend/src backend/src
RUN mvn -pl backend -am package -DskipTests -q

# ── Etapa 2: imagen de ejecución ─────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/backend/target/nutrifit-backend-0.1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
