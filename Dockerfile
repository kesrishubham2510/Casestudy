FROM eclipse-temurin:17-jdk

# TODO: Remove these steps, these are redundant
# copy all the complete folder into a place in ubuntu image
RUN mkdir -p /app/jar

COPY ./build/libs/covidstat-0.0.1-SNAPSHOT.jar /app/jar/libs

RUN  echo pwd

WORKDIR /app/jar/libs

EXPOSE 8080

# Add an entry command, this command runs when the container spins up
ENTRYPOINT ["/bin/sh", "-c", "java -Dspring.profiles.active=prod -jar covidstat-0.0.1-SNAPSHOT.jar"]