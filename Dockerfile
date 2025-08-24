FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY target/booking-rent-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java","-jar","app.jar","--spring.config.location=classpath:/application.properties"]

