FROM hyperledger/besu:25.2.1
COPY --chown=besu:besu core/build/libs/*.jar  /opt/besu/plugins/

EXPOSE 30303 8545 8551 8546 8547 8888 9545 8008 5051 9090 9000/udp 9000

ENV HOST_IP=127.0.0.1

ENV BESU_LOGGING=INFO \
    BESU_NETWORK=HOLESKY \
    BESU_SYNC_MODE=FAST \
    BESU_SYNC_MIN_PEERS=1 \
    BESU_METRICS_ENABLED=true \
    BESU_DATA_PATH=/opt/besu/data

ENTRYPOINT besu --rpc-http-enabled --rpc-http-api=ADMIN,ETH,SAMBA --plugin-samba-host=$HOST_IP --rpc-http-host=0.0.0.0
#"--logging=DEBUG"