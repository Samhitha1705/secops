FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY target/devsecops-demo-1.0.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java","-jar","app.jar"]
