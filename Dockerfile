#https://codefresh.io/docs/docs/learn-by-example/java/gradle/
FROM gradle:8.2.1-jdk17-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build -x test --no-daemon --stacktrace

FROM eclipse-temurin:17.0.7_7-jre-jammy
EXPOSE 8080
RUN mkdir /app
RUN apt-get update && \
    apt-get install -y libglib2.0-0 libnss3 libnspr4 libatk1.0-0 libatk-bridge2.0-0 libcups2 libdrm2 && \
    apt-get install -y libdbus-1-3 libxcb1 libxkbcommon0 libatspi2.0-0 libx11-6 libxcomposite1 libxdamage1 && \
    apt-get install -y libxext6 libxfixes3 libxrandr2 libgbm1 libpango-1.0-0 libcairo2 libasound2
COPY --from=build /home/gradle/src/build/libs/*.jar /app/spring-boot-application.jar
#COPY --from=build /home/gradle/src/build/libs/api-vendor-0.0.1-SNAPSHOT.jar /app/spring-boot-application.jar

#ENTRYPOINT ["java","-jar","/app/spring-boot-application.jar"]
#https://stackoverflow.com/questions/44491257/how-to-reduce-spring-boot-memory-usage
ENTRYPOINT ["java","-Xmx512m","-Xss512k","-XX:+UseSerialGC","-XX:MaxRAM=72m","-jar","/app/spring-boot-application.jar"]
#ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-Djava.security.egd=file:/dev/./urandom","-jar","/app/spring-boot-application.jar"]