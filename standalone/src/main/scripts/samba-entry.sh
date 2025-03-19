#!/bin/bash

# Construct the command as a single string
COMMAND="/opt/samba/bin/samba $@"

# Check if current user is not root. If not, run the command as is.
if [ "$(id -u)" -ne 0 ]; then
    exec /bin/bash -c "$COMMAND"
fi

# Run Samba first to get paths needing permission adjustment
output=$(/opt/samba/bin/samba --print-paths-and-exit $SAMBA_USER_NAME "$@")

# Parse the output to find the paths and their required access types
echo "$output" | while IFS=: read -r prefix path accessType; do
    if [[ "$prefix" == "PERMISSION_CHECK_PATH" ]]; then
      # Change ownership to samba user and group
      chown -R $SAMBA_USER_NAME:$SAMBA_USER_NAME $path

      # Ensure read/write permissions for samba user

      echo "Setting permissions for: $path with access: $accessType"

      if [[ "$accessType" == "READ" ]]; then
        # Set read-only permissions for samba user
        # Add execute for directories to allow access
        find $path -type d -exec chmod u+rx {} \;
        find $path -type f -exec chmod u+r {} \;
      elif [[ "$accessType" == "READ_WRITE" ]]; then
        # Set read/write permissions for samba user
        # Add execute for directories to allow access
        find $path -type d -exec chmod u+rwx {} \;
        find $path -type f -exec chmod u+rw {} \;
      fi
    fi
done

# Switch to the samba user and execute the command
exec su -s /bin/bash "$SAMBA_USER_NAME" -c "$COMMAND"
