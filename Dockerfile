FROM eclipse-temurin:17-jdk AS build

WORKDIR /app
COPY . .

RUN chmod +x gradlew
RUN ./gradlew clean bootJar -x test

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

CMD ["java", "-jar", "app.jar"]