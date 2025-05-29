FROM meldsun/besu:discovery
#FROM meldsun/besu:discovery-arm64
COPY --chown=besu:besu core/build/libs/*.jar  /opt/besu/plugins/

EXPOSE 30303 8545 8551 8546 8547 8888 9545 8008 5051 9090 9000/udp 9000

ENV HOST_IP=0.0.0.0
ENV LOG_LEVEL=INFO

ENV BESU_LOGGING=INFO \
    BESU_NETWORK=HOLESKY \
    BESU_SYNC_MODE=FAST \
    BESU_SYNC_MIN_PEERS=1 \
    BESU_DATA_PATH=/opt/besu/data

COPY /docker/entrypoint.sh /opt/besu/entrypoint.sh
RUN chmod +x /opt/besu/entrypoint.sh
ENTRYPOINT ["/opt/besu/entrypoint.sh"]