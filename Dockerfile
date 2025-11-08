# Многоэтапная сборка для оптимизации размера образа
FROM gradle:8.6-jdk21-alpine AS build
WORKDIR /app

# Копируем gradle файлы для кэширования зависимостей
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Загружаем зависимости (будет закэшировано)
RUN gradle dependencies --no-daemon

# Copy source code
COPY src ./src

# Build application
RUN gradle bootJar --no-daemon

# Final image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy jar from build stage
COPY --from=build /app/build/libs/*.jar everage.jar

# Expose port
EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "everage.jar"]