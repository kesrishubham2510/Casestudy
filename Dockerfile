FROM eclipse-temurin:17-jre

# copy all the complete folder into a place in ubuntu image
RUN mkdir -p /app/jar

COPY ./build/libs/covidstat-0.0.1-SNAPSHOT.jar /app/jar

RUN  echo pwd
RUN wget https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar


WORKDIR /app/jar

EXPOSE 8080

# Add an entry command, this command runs when the container spins up
ENTRYPOINT ["java",
  "-javaagent:/app/opentelemetry-javaagent.jar",
  "-Dotel.instrumentation.logback-mdc.enabled=true",
  "-Dspring.profiles.active=prod",
  "-jar",
  "/app/app.jar"
]