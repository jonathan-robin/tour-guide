# Étape 1: Construire l'application avec Maven
FROM maven:3.8.6-openjdk-17-slim AS build

# Définir le répertoire de travail
WORKDIR /app

# Copier le fichier pom.xml et télécharger les dépendances
COPY pom.xml .
RUN mvn dependency:go-offline

# Copier le code source de l'application
COPY src /app/src

# Construire l'application avec Maven (génère le JAR)
RUN mvn clean package -DskipTests

# Étape 2: Créer l'image d'exécution
FROM openjdk:17-alpine

# Définir le répertoire de travail
WORKDIR /app

# Copier le JAR généré depuis l'étape de construction
COPY --from=build /app/target/*.jar app.jar

# Exposer le port sur lequel l'application va s'exécuter
EXPOSE 8080

# Lancer l'application Java (lancer le JAR avec OpenJDK)
ENTRYPOINT ["java", "-jar", "/app/app.jar"]