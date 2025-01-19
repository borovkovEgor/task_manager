FROM openjdk:17-jdk
WORKDIR /app
COPY target/message-brokers-0.0.1-SNAPSHOT.jar /app/message-broker.jar
EXPOSE 8081
CMD ["java", "-jar", "message-broker.jar"]