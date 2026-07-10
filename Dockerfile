FROM eclipse-temurin:25-jre

EXPOSE 8080

COPY backend/target/streamscout.jar streamscout.jar

ENTRYPOINT ["java","-jar","/streamscout.jar"]