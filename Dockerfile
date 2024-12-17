FROM eclipse-temurin:21 as BUILD
WORKDIR /build
COPY . .
RUN ./mvnw -DskipTests clean package

FROM eclipse-temurin:21-jre
COPY --from=BUILD /build/target /opt/app
WORKDIR /opt/app
CMD ["java", "-jar", "kvk-assessment-0.0.1-SNAPSHOT.jar"]
