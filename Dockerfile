# Dockerfile for universalregistrar/driver-did-factom

FROM adoptopenjdk/openjdk11:jre
MAINTAINER Sphereon Dev <dev@sphereon.com>

# Default testnet node using Factom OpenNode
ENV NODE1_ENABLED true
ENV NODE1_NETWORK_ID testnet
ENV NODE1_FACTOMD_URL http://ams-test01.blockchain-innovation.org:8088/v2
ENV NODE1_ES_ADDRESS Es3Y6U6H1Pfg4wYag8VMtRZEGuEJnfkJ2ZuSyCVcQKweB6y4WvGH

#
# Additional nodes can be passed in using environment variables

EXPOSE 9080

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
