FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8086
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
