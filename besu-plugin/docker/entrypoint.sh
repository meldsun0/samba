#!/bin/bash
#echo "[Samba Boot] LOG_LEVEL is $LOG_LEVEL"
exec besu \
  --rpc-http-enabled \
  --rpc-http-api=ADMIN,ETH,SAMBA \
  --metrics-enabled \
  --metrics-host=0.0.0.0 \
  --rpc-http-host=0.0.0.0 \
  --host-allowlist="*" \
  --plugin-samba-host="$HOST_IP" \
  --plugin-samba-data-path="$BESU_DATA_PATH/samba" \
  --plugin-samba-logging="$LOG_LEVEL"
