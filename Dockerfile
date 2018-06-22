FROM openjdk:8-jdk-alpine

ARG ENVIRONMENT=prod

COPY build/libs/limbo-1.0.0-SNAPSHOT.jar /app.jar
COPY .docker/app.sh /app.sh

WORKDIR /app

ENTRYPOINT ["/app.sh"]

ENV ENVIRONMENT=$ENVIRONMENT
EXPOSE 43596
