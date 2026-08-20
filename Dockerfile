# Build
FROM maven:3.9.16-eclipse-temurin-21 AS builder
WORKDIR /app
#so now copy the maven configuration first -> allowing the docker to cache the dependencies
# . -> the current directory
COPY pom.xml .

RUN mvn dependency:go-offline
COPY src ./src
#Build the spring boot application
# Skip the tests to speed up the build process
RUN mvn package -DskipTests

# Rename the generated JAR file to a standard name (app.jar) for easier reference in the runtime stage
RUN mv target/*.jar target/app.jar


# Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
# Copy the jar file from the builder stage to the runtime stage
# the first which is mentioned is the source and the second is the destination
COPY --from=builder /app/target/app.jar app.jar

# Expose the port that the application will run on
EXPOSE 8080
# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]


