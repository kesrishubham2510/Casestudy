FROM eclipse-temurin:17-jdk

# TODO: Remove these steps, these are redundant
# copy all the complete folder into a place in ubuntu image
RUN mkdir -p /app/jar

WORKDIR /var/jenkins_home/workspace/covidstat/build/libs
RUN cp -r *.jar /app/jar

WORKDIR /app/jar/libs

EXPOSE 8080

# Add an entry command, this command runs when the container spins up
ENTRYPOINT ["/bin/sh", "-c", "java -Dspring.profiles.active=prod -jar covidstat-0.0.1-SNAPSHOT.jar"]