# Utiliser l'image de base JDK 17
FROM eclipse-temurin:17-jdk-alpine

# Installer Maven dans l'image
RUN apk update && apk add --no-cache maven

# Répertoire de travail dans le conteneur
WORKDIR /app

# Copier les fichiers JAR locaux dans le conteneur
COPY libs/gpsUtil.jar /libs/gpsUtil.jar
COPY libs/RewardCentral.jar /libs/RewardCentral.jar
COPY libs/TripPricer.jar /libs/TripPricer.jar

# Installer les fichiers JAR dans le répertoire local de Maven
RUN mvn install:install-file -Dfile=/libs/gpsUtil.jar -DgroupId=gpsUtil -DartifactId=gpsUtil -Dversion=1.0.0 -Dpackaging=jar
RUN mvn install:install-file -Dfile=/libs/RewardCentral.jar -DgroupId=rewardCentral -DartifactId=rewardCentral -Dversion=1.0.0 -Dpackaging=jar
RUN mvn install:install-file -Dfile=/libs/TripPricer.jar -DgroupId=tripPricer -DartifactId=tripPricer -Dversion=1.0.0 -Dpackaging=jar


# Copier le fichier pom.xml et les dépendances Maven
COPY pom.xml .

# Télécharger les dépendances Maven sans exécuter les tests
RUN mvn dependency:go-offline

# Copier le reste de votre application dans l'image Docker
COPY src /app/src

# Construire votre application avec Maven (en ignorant les tests)
RUN mvn clean package -DskipTests

# Spécifier la commande pour démarrer votre application
CMD ["java", "-jar", "target/mon-application.jar"]