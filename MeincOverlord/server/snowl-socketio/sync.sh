#!/bin/bash

SCRIPT_DIR="$(dirname "${BASH_SOURCE[0]}")"

if [ "$1" ]; then
    SYNC_TO="$1"
else
    echo "Usage: $0 <sync-to-dir>" >&2
    exit 1
fi

if [ ! -d "$SYNC_TO" ]; then
    echo "Directory not found: $SYNC_TO" >&2
    exit 1
fi

echo -e "\nSyncing Socket IO configuration files to $SYNC_TO\n"

rsync -ai "$SCRIPT_DIR/app.js" \
          "$SYNC_TO/app/"
rsync -ai "$SCRIPT_DIR/index.html" \
          "$SYNC_TO/app/"
