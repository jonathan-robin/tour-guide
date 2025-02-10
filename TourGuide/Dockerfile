# Utiliser l'image de base JDK 17
FROM eclipse-temurin:17-jdk-alpine

# Installer Maven
RUN apk add --no-cache maven

# Répertoire de travail dans le conteneur
WORKDIR /app

# Copier les fichiers JAR locaux dans le conteneur
COPY libs/gpsUtil.jar /app/libs/gpsUtil.jar
COPY libs/RewardCentral.jar /app/libs/RewardCentral.jar
COPY libs/TripPricer.jar /app/libs/TripPricer.jar

# Installer les fichiers JAR dans le répertoire local de Maven
RUN mvn install:install-file -Dfile=/app/libs/gpsUtil.jar -DgroupId=gpsUtil -DartifactId=gpsUtil -Dversion=1.0.0 -Dpackaging=jar
RUN mvn install:install-file -Dfile=/app/libs/RewardCentral.jar -DgroupId=rewardCentral -DartifactId=rewardCentral -Dversion=1.0.0 -Dpackaging=jar
RUN mvn install:install-file -Dfile=/app/libs/TripPricer.jar -DgroupId=tripPricer -DartifactId=tripPricer -Dversion=1.0.0 -Dpackaging=jar

# Copier le fichier pom.xml
COPY pom.xml .

# Copier le reste de votre application dans l'image Docker
COPY src /app/src

# Construire votre application avec Maven
RUN mvn clean package -DskipTests

# Spécifier la commande pour démarrer votre application
CMD ["java", "-jar", "target/*.jar"]