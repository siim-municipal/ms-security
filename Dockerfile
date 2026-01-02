# --- ETAPA 1: BUILD (Compilación) ---
# Usamos una imagen que tiene Maven y Java 21 instalados
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# 1. Copiamos solo el pom.xml primero para aprovechar la caché de Docker
# Si no cambias dependencias, Docker no volverá a descargar internet entero
COPY pom.xml .
# Descargamos dependencias (esto crea una capa cacheada)
RUN mvn dependency:go-offline -B

# 2. Copiamos el código fuente
COPY src ./src

# 3. Compilamos el proyecto y generamos el JAR (Saltando tests para agilizar despliegue)
RUN mvn clean package -DskipTests

# --- ETAPA 2: RUNTIME (Ejecución) ---
# Usamos una imagen ligera solo con JRE para correr la app
FROM eclipse-temurin:21-jre-alpine

# Creamos usuario por seguridad (igual que tenías)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

WORKDIR /app

# Copiamos SOLO el war compilado desde la Etapa 1
# Nota: El nombre del war puede variar, usamos un wildcard o el nombre específico
COPY --from=build /app/target/*.war app.war

EXPOSE 8080

# Configuración de JVM para contenedores (detecta RAM disponible automáticamente)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.war"]