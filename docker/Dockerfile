# Dockerfile for universalregistrar/driver-did-factom

FROM universalregistrar/base-ubuntu
MAINTAINER Sphereon Dev <dev@sphereon.com>

ARG GITHUB_READ_PACKAGES_OWNER
ARG GITHUB_READ_PACKAGES_TOKEN

# Default testnet node using Factom OpenNode
ENV NODE1_ENABLED true
ENV NODE1_NETWORK_ID testnet
ENV NODE1_FACTOMD_URL http://localhost:8088/v2
ENV NODE1_ES_ADDRESS Es4JHJ7T2E34j2Xqg84jWZRvgJ1cBtZZMseL2GxaEwJ7PigV23dh

# Default mainnet node using Factom OpenNode
ENV NODE2_ENABLED false
ENV NODE2_NETWORK_ID mainnet
ENV NODE2_FACTOMD_URL http://localhost:8088/v2

#
# Additional nodes can be passed in using environment variables

# done

ADD ./settings.xml /root/.m2/
ADD . /opt/driver-did-factom
RUN cd /opt/driver-did-factom && mvn --settings settings.xml clean install package -N -DskipTests

EXPOSE 9080

RUN chmod 555 /opt/driver-did-factom/docker/run-driver-did-factom.sh
CMD "/opt/driver-did-factom/docker/run-driver-did-factom.sh"
