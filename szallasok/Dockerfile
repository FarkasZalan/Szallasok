FROM openjdk:17-jdk

WORKDIR /app

COPY target/szallasokApp.jar szallasokApp.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "szallasokApp.jar"]
