FROM gradle:6.2.2-jdk8 as builder
RUN mkdir /backend
WORKDIR /backend
COPY . /backend
RUN chmod +x /backend/gradlew && ./gradlew bootJar

RUN find /backend


FROM openjdk:11-jre-slim-buster as dist
RUN mkdir /hnh && mkdir /hnh/database && mkdir /hnh/public
WORKDIR /hnh
RUN apt update && apt install -y pngquant 
COPY --from=builder /backend/build/libs/map-backend.jar map-backend.jar
RUN groupadd --gid 1001 maper && useradd --uid 1001 --gid maper --shell /bin/bash --create-home maper
ENV HUMIO_TOKEN="abcd"
ENV MAP_USER="TestUser"
ENV MAP_PASSWORD="TestPassword"
ENV MAP_ADMIN_TOKEN="abcd"

COPY entrypoint.sh .
EXPOSE 5000

RUN chown -R 1001:1001 /hnh
USER 1001:1001
VOLUME ["/hnh/database"]
VOLUME ["/hnh/public"]

ENTRYPOINT ["/bin/sh", "entrypoint.sh"]