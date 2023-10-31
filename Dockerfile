FROM openjdk:22-jdk
COPY target/AFT-0.0.1-SNAPSHOT.jar AFT.jar
ENTRYPOINT ["java","-jar","/AFT.jar"]