FROM amazoncorretto:17-alpine-jdk
COPY target/*.jar payment.jar
ENTRYPOINT ["java","-jar","/payment.jar"]