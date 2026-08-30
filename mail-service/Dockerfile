FROM amazoncorretto:25

COPY /build/libs/mail-service-0.0.1-SNAPSHOT.jar /app/
WORKDIR /app/
RUN mv /app/*.jar /app/app.jar
ENTRYPOINT ["java"]
CMD ["-jar", "app.jar"]