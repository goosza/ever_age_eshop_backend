# Многоэтапная сборка для оптимизации размера образа
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY everage.jar everage.jar

# Expose port
EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "everage.jar"]