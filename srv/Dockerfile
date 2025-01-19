FROM openjdk:17-jdk
WORKDIR /app
COPY build/libs/srv-0.0.1-SNAPSHOT.jar /app/srv.jar
EXPOSE 8080
CMD ["java", "-jar", "srv.jar"]
