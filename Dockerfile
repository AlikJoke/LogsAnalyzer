FROM gradle:8.0.2-jdk19
RUN mkdir -p /app
WORKDIR /app
COPY build.gradle /app
COPY src /app/src
RUN gradle bootJar --exclude-task :test --no-daemon

FROM openjdk:19
MAINTAINER Alik <typicaljprogrammer@gmail.com>
COPY build/libs/*.jar logs-analyzer.jar
EXPOSE 8080
ENTRYPOINT ["java", "--enable-preview", "-jar", "logs-analyzer.jar"]
