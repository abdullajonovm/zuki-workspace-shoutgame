#!/bin/bash

function log {
    echo "$(date "+%Y/%m/%d %H:%M:%S") socketio: $1"
}

log "Starting daemon"

set -e

exec /usr/local/sbin/pre-launch.sh \
    -D /app \
    -u $SOCKETIO_USER \
    -c "node app.js"
