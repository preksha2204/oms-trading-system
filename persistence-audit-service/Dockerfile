FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8088
ENV JAVA_OPTS="-XX:+UseZGC -Xmx2g -Xms512m"
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
