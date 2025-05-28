#!/bin/bash

# Construct the command to run Samba with passed arguments
COMMAND="/opt/samba/bin/samba $@"

# If not running as root, just execute the command
if [ "$(id -u)" -ne 0 ]; then
  exec $COMMAND
fi

# Run Samba in permission-discovery mode
output=$(/opt/samba/bin/samba --print-paths-and-exit "$SAMBA_USER_NAME" "$@")

# Parse permission hints and adjust ownership/permissions
echo "$output" | while IFS=':' read -r prefix path accessType; do
  if [[ "$prefix" == "PERMISSION_CHECK_PATH" ]]; then
    echo "Setting permissions for: $path (access: $accessType)"
    chown -R "$SAMBA_USER_NAME:$SAMBA_USER_NAME" "$path"

    if [[ "$accessType" == "READ" ]]; then
      find "$path" -type d -exec chmod u+rx {} \;
      find "$path" -type f -exec chmod u+r {} \;
    elif [[ "$accessType" == "READ_WRITE" ]]; then
      find "$path" -type d -exec chmod u+rwx {} \;
      find "$path" -type f -exec chmod u+rw {} \;
    fi
  fi
done

exec $COMMAND
