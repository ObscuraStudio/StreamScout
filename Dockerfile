FROM eclipse-temurin:25-jre

RUN groupadd --system streamscout && useradd --system --gid streamscout streamscout

EXPOSE 8080

COPY backend/target/streamscout.jar streamscout.jar

RUN chown streamscout:streamscout /streamscout.jar
USER streamscout

ENTRYPOINT ["java","-jar","/streamscout.jar"]
