# Этап 1: сборка jar-файла
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
# Страховка: на Windows скрипт может прийти с лишними символами конца строки
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
COPY src src
# Тесты не запускать: им нужен Docker, а внутри сборки его нет
RUN ./mvnw -q -DskipTests package

# Этап 2: лёгкий образ только с Java и готовым приложением
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]