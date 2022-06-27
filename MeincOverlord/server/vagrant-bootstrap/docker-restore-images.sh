#!/bin/bash

set -e 

if [ "$(id -u)" -ne 0 ]; then
    echo "Must be root" >&2
    exit 1
fi

BACKUP_DIR="/mnt/host/docker-images-backups"

RESTORE_FILE="$1"
if [ ! "$RESTORE_FILE" ]; then
    RESTORE_FILE="$(ls -1t $BACKUP_DIR/*.tar | head -n 1)"
    if [ ! "$RESTORE_FILE" ]; then
        echo "Cannot find backup file in $BACKUP_DIR" >&2
        exit 1
    fi
    read -rp "Restore docker images from $RESTORE_FILE? (y/N) " USER_INPUT
    if [ "${USER_INPUT,,}" != "y" ]; then
        exit 0
    fi
fi

echo "Restoring docker images from $RESTORE_FILE"
docker load -i "$RESTORE_FILE"
echo "Restore finished"
