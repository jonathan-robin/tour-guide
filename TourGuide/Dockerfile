# Utiliser l'image de base JDK 17
FROM eclipse-temurin:17-jdk-alpine

# Répertoire de travail dans le conteneur
WORKDIR /app

# Copier le fichier pom.xml et les dépendances Maven
COPY pom.xml .
RUN mvn dependency:go-offline

# Copier le reste de votre application dans l'image Docker
COPY src /app/src

# Construire votre application avec Maven
RUN mvn clean package -DskipTests

# Spécifier la commande pour démarrer votre application
CMD ["java", "-jar", "target/mon-application.jar"]
