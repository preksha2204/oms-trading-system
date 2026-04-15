FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8083
ENV JAVA_OPTS="-XX:+UseZGC -Xmx4g -Xms1g"
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
