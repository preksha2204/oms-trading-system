FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseZGC -Xmx2g -Xms512m -XX:+AlwaysPreTouch"
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
