FROM ubuntu:24.04
ARG VERSION="dev"
ENV NO_PROXY_CACHE="-o Acquire::BrokenProxy=true -o Acquire::http::No-Cache=true -o Acquire::http::Pipeline-Depth=0"

# Update and install dependencies without using any cache
RUN apt-get update $NO_PROXY_CACHE  && \
  # $NO_PROXY_CACHE must not be used here or otherwise will trigger a hadolint error
  apt-get -o Acquire::BrokenProxy=true -o Acquire::http::No-Cache=true -o Acquire::http::Pipeline-Depth=0 \
    --no-install-recommends -q --assume-yes install openjdk-21-jre-headless=21* libjemalloc-dev=5.* adduser=3*  && \
  # Clean apt cache
  apt-get clean  && \
  rm -rf /var/cache/apt/archives/* /var/cache/apt/archives/partial/*  && \
  rm -rf /var/lib/apt/lists/*  && \
  # Starting from version 23.10, Ubuntu comes with an "ubuntu" user with uid 1000. We need 1000 for samba.
  userdel ubuntu 2>/dev/null || true && rm -rf /home/ubuntu  && \
  # Ensure we use a stable UID for samba, as file permissions are tied to UIDs.
  adduser --uid 1000 --disabled-password --gecos "" --home /opt/samba samba  && \
  chown samba:samba /opt/samba  && \
  chmod 0755 /opt/samba

RUN apt-get update && apt-get install -y gosu && rm -rf /var/lib/apt/lists/*

ARG SAMBA_USER=samba
USER ${SAMBA_USER}
WORKDIR /opt/samba

COPY --chown=samba:samba samba /opt/samba/
# Make sure logs/ exists and is writable by the samba user
RUN mkdir -p /opt/samba/logs && \
    chown samba:samba /opt/samba/logs && \
    chmod 755 /opt/samba/logs

#     JSON-RPC Metrics, Rest API,Discv5
EXPOSE 8545 8008 5051 9090 9000/udp 9000

ENV SAMBA_PID_PATH "/tmp/pid"

ENV OTEL_RESOURCE_ATTRIBUTES="service.name=samba,service.version=$VERSION"

ENV OLDPATH="${PATH}"
ENV PATH="/opt/samba/bin:${OLDPATH}"


# The entry script just sets permissions as needed based on samba config
# and is replaced by the samba process running as samba user.
# Suppressing this warning as there's no risk here because the root user
# only sets permissions and does not continue running the main process.
# hadolint ignore=DL3002
USER root
RUN chmod +x /opt/samba/bin/samba-entry.sh

USER samba
ENV SAMBA_USER_NAME=${SAMBA_USER}
ENTRYPOINT ["samba-entry.sh"]

# Build-time metadata as defined at http://label-schema.org
ARG BUILD_DATE
ARG VCS_REF
LABEL org.label-schema.build-date=$BUILD_DATE \
      org.label-schema.name="Samba" \
      org.label-schema.description="Samba Portal Client" \
      org.label-schema.url="https://github.com/meldsun0/samba" \
      org.label-schema.vcs-ref=$VCS_REF \
      org.label-schema.vcs-url="https://github.com/meldsun0/samba.git" \
      org.label-schema.vendor="Meldsun Labs" \
      org.label-schema.version=$VERSION \
      org.label-schema.schema-version="1.0"
