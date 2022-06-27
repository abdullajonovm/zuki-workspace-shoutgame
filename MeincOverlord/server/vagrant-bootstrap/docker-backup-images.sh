#!/bin/bash

set -e 

if [ "$(id -u)" -ne 0 ]; then
    echo "Must be root" >&2
    exit 1
fi

BACKUP_DATE_STRING="$(date +%Y%m%d-%H%M%S)"
BACKUP_FILE="/mnt/host/docker-images-backups/images-backup-$BACKUP_DATE_STRING.tar"
echo "Backing up Docker images to $BACKUP_FILE"
DOCKER_IMAGES=$(docker images | tail -n +2 | grep -v "<none>" | awk '{print $1 ":" $2}')
docker save -o "$BACKUP_FILE" $DOCKER_IMAGES
echo "Backup finished"
