#!/bin/bash
#echo "[Samba Boot] LOG_LEVEL is $LOG_LEVEL"
exec besu \
  --rpc-http-enabled \
  --rpc-http-api=ADMIN,ETH,SAMBA \
  --plugin-samba-host="$HOST_IP" \
  --rpc-http-host=0.0.0.0 \
  --plugin-samba-data-path="$BESU_DATA_PATH/samba" \
  --plugin-samba-logging="$LOG_LEVEL"
