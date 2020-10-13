FROM openjdk:8-alpine

COPY target/uberjar/case-plan-server.jar /case-plan-server/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/case-plan-server/app.jar"]
