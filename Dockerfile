# build: compila el jar dentro del propio contenedor, no depende de que alguien
# haya corrido mvnw en su maquina antes de buildear la imagen
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /build

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -B -q dependency:go-offline

COPY src/ src/
RUN ./mvnw -B -q package -DskipTests

# runtime: imagen mas chica, solo el jar y el JRE, sin JDK ni fuentes
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
COPY --from=build /build/target/*.jar app.jar
USER app

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
