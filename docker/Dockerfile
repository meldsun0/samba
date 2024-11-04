FROM eclipse-temurin:21 as jre-build

FROM ubuntu:24.04
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH "${JAVA_HOME}/bin:${PATH}"
COPY --from=jre-build /javaruntime $JAVA_HOME

RUN apt-get -y update && apt-get -y upgrade && apt-get -y install curl libc-bin libc6 adduser && \
    # Clean apt cache
    apt-get clean && \
    rm -rf /var/cache/apt/archives/* && \
    rm -rf /var/lib/apt/lists/*

# Ubuntu 23.10 and above comes with an "ubuntu" user with uid 1000. We need 1000 for samba.
RUN userdel ubuntu 2>/dev/null || true && rm -rf /home/ubuntu  && \
    adduser --uid 1000 --disabled-password --gecos "" --home /opt/samba samba && \
    chown samba:samba /opt/samba && \
    chmod 0755 /opt/samba

USER samba
WORKDIR /opt/samba

# copy application (with libraries inside)
COPY --chown=samba:samba samba /opt/samba/

ENV LANG C.UTF-8

ENV SAMBA_REST_API_INTERFACE="0.0.0.0"
ENV SAMBA_METRICS_INTERFACE="0.0.0.0"
ENV PATH "/opt/samba/bin:${PATH}"


EXPOSE 8545 9009/udp
# specify default command
ENTRYPOINT ["/opt/samba/bin/samba"]
