FROM docker.io/library/maven:3-eclipse-temurin-17 AS builder
WORKDIR /build
COPY . ./
RUN mvn -B -s settings.xml package

FROM docker.io/library/eclipse-temurin:17
WORKDIR /opt/app
RUN chmod 770 /opt/app
COPY --from=builder /build/target/eclipsecon.jar ./
CMD [ "java", "-jar", "eclipsecon.jar" ]
