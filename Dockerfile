FROM maven:3.9.9-eclipse-temurin-21
WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "target/nakitera-case-study-0.0.1-SNAPSHOT.jar"]