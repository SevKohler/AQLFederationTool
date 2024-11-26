FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

ARG MAVEN_OPTS="-Dhttp.proxyHost=${http_proxy_host} -Dhttp.proxyPort=${http_proxy_port} -Dhttps.proxyHost=${https_proxy_host} -Dhttps.proxyPort=${https_proxy_port}"

COPY pom.xml ./
RUN mvn dependency:go-offline

COPY . ./
RUN mvn clean package -DskipTests


FROM eclipse-temurin:21

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081/tcp
ENTRYPOINT ["java", "-jar", "app.jar"]
