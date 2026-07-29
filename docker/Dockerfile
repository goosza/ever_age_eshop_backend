# Stage 1: Extract JAR layers
FROM eclipse-temurin:21-jre-alpine AS builder
WORKDIR /app
COPY everage.jar everage.jar
RUN java -Djarmode=tools -jar everage.jar extract --layers --launcher --destination extracted

# Stage 2: Final image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Security: non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy layers in order of change frequency (least → most)
# dependencies change rarely → cached aggressively
COPY --from=builder /app/extracted/dependencies/ ./
COPY --from=builder /app/extracted/spring-boot-loader/ ./
COPY --from=builder /app/extracted/snapshot-dependencies/ ./
# application code changes often → separate layer
COPY --from=builder /app/extracted/application/ ./

EXPOSE 8080

HEALTHCHECK --interval=120s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
