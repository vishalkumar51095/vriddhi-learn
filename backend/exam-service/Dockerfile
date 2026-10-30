FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN useradd --system --create-home appuser
COPY --from=build /workspace/target/exam-service-*.jar app.jar
USER appuser
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
