FROM eclipse-temurin:17-jdk

# copy all the complete folder into a place in ubuntu image
RUN mkdir -p /home/developer/casestudy/covidstat
RUN mkdir -p /app/jar

COPY ./ /home/developer/casestudy/covidstat

# navigate to the repo and run ./gradlew build
WORKDIR ./home/developer/casestudy/covidstat

# pick up the .jar file from the /build folder
RUN chmod +x gradlew
RUN ./gradlew build

RUN cp -r ./build/libs /app/jar

WORKDIR /app/jar/libs

EXPOSE 8080

# Add an entry command, this command runs when the container spins up
ENTRYPOINT ["/bin/sh", "-c", "java -Dspring.profiles.active=prod -jar covidstat-0.0.1-SNAPSHOT.jar"]