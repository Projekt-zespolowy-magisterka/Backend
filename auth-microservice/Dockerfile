FROM openjdk:19-jdk-slim
WORKDIR /app
COPY target/auth-microservice-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]