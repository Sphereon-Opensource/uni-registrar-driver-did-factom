# Dockerfile for universalregistrar/driver-did-factom

FROM adoptopenjdk/openjdk11:jre
MAINTAINER Sphereon Dev <dev@sphereon.com>

# Default testnet node using Factom OpenNode
ENV NODE1_ENABLED true
ENV NODE1_NETWORK_ID testnet
ENV NODE1_FACTOMD_URL https://dev.factomd.net/v2
ENV NODE1_EC_ADDRESS <entry-credit-secret-address>

#
# Additional nodes can be passed in using environment variables

EXPOSE 9080

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
