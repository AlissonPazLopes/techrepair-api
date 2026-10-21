# Estágio 1: Build da aplicação com Maven e Java 21
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copia o pom.xml e baixa as dependências para aproveitar o cache de camadas do Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código-fonte e empacota o artefato .jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# Estágio 2: Imagem enxuta de execução com Eclipse Temurin JRE 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Cria usuário não-root por segurança
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copia o .jar compilado do estágio de build
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
