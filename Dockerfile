FROM openjdk:8-jdk-alpine
# add user
RUN groupadd spring
RUN useradd spring -g spring -m -s /bin/bash
RUN echo "spring:s3cr3t@1234" | chpasswd

USER spring:spring

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENV JAVA_OPTS=""

ENTRYPOINT [ "sh", "-c", "exec java $JAVA_OPTS -jar /app.jar" ]
