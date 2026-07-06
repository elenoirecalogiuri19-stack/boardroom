# ============================================================
# Boardroom – Dockerfile 3-stage
#
# Stage 1 (frontend): Node 22 compila Angular → target/classes/static/
# Stage 2 (backend):  Maven impacchetta il JAR senza eseguire npm
# Stage 3 (runtime):  JRE 17 minimale, utente non-root
# ============================================================

# ── Stage 1: build Angular ───────────────────────────────────
FROM node:22-slim AS frontend
WORKDIR /app

# Installa dipendenze npm (layer cached separato dal sorgente)
COPY package.json package-lock.json ./
RUN npm ci --prefer-offline

# Configurazione Angular / Webpack / Service Worker
COPY angular.json tsconfig.json tsconfig.app.json ngsw-config.json ./
COPY webpack webpack/

# Sorgente Angular
COPY src/main/webapp src/main/webapp/

# Build produzione → scrive in target/classes/static/
RUN npm run webapp:prod

# ── Stage 2: build Java ──────────────────────────────────────
FROM eclipse-temurin:17-jdk-focal AS backend
WORKDIR /build

COPY mvnw pom.xml sonar-project.properties ./
COPY .mvn .mvn
# Rimuove i CRLF di Windows dal wrapper script
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Pre-fetch dipendenze Maven (layer cached; invalida solo se pom.xml cambia)
RUN ./mvnw dependency:go-offline -B -q -Pprod || true

# Sorgente Java e risorse
COPY src src

# Inietta gli asset Angular già compilati nello stage precedente.
# Maven li troverà in target/classes/static/ e li includerà nel JAR.
COPY --from=frontend /app/target/classes/static target/classes/static

# Build JAR senza rieseguire Node.js:
#   -Dskip.installnodenpm  → non scarica Node/npm
#   -Dskip.npm             → non esegue npm install né webapp:prod
RUN ./mvnw -Pprod -DskipTests -Dskip.installnodenpm -Dskip.npm -Dmodernizer.skip=true package -B -q

# ── Stage 3: runtime ─────────────────────────────────────────
FROM eclipse-temurin:17-jre-focal
LABEL maintainer="boardroom.progetto@gmail.com"
LABEL description="Boardroom – sistema prenotazione sale riunioni"

RUN groupadd --system --gid 1000 spring && \
    useradd --system --uid 1000 --gid spring spring

WORKDIR /app
COPY --from=backend --chown=spring:spring /build/target/*.jar app.jar
USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
