FROM openjdk:21
MAINTAINER org.example
COPY target/demo-0.0.1-SNAPSHOT.jar appLucian.jar
ENTRYPOINT ["java","-jar","/app.jar"]