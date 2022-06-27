#!/bin/bash

cd "$MRSOA_HOME"

DIRS_TO_LINK="state"
for DIR_TO_LINK in $DIRS_TO_LINK; do
    HOST_DIR="/mnt/host/$NOMAD_ALLOC_INDEX/$DIR_TO_LINK"
    mkdir -p "$HOST_DIR"
    chown -R "$MRSOA_USER:$MRSOA_GROUP" "$HOST_DIR"
    chmod 750 "$HOST_DIR"
    if [ -e "$DIR_TO_LINK" ]; then
        if readlink "$DIR_TO_LINK" >/dev/null; then
            rm "$DIR_TO_LINK"
        else
            rmdir "$DIR_TO_LINK"
        fi
    fi
    ln -sf "$HOST_DIR"
done
