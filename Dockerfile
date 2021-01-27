# Dockerfile for universalregistrar/driver-did-factom

FROM adoptopenjdk/openjdk11:jre
MAINTAINER Sphereon Dev <dev@sphereon.com>

# Default testnet node using Factom OpenNode
ENV NODE1_ENABLED true
ENV NODE1_NETWORK_ID testnet
ENV NODE1_FACTOMD_URL https://a26cc2717826.ngrok.io/v2
ENV NODE1_ES_ADDRESS Es4JHJ7T2E34j2Xqg84jWZRvgJ1cBtZZMseL2GxaEwJ7PigV23dh

#
# Additional nodes can be passed in using environment variables

EXPOSE 9080

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
