FROM amazoncorretto:17-alpine

WORKDIR /app

# Create upload directory
RUN mkdir -p ./uploads/user-service

COPY target/user-service-1.0.0.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
