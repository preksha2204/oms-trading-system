FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8087
ENV JAVA_OPTS="-Xmx1g -Xms512m"
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
