FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8082
ENV JAVA_OPTS="-XX:+UseZGC -Xmx8g -Xms2g -XX:+AlwaysPreTouch -XX:ConcGCThreads=4"
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
