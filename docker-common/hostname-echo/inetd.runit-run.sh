#!/bin/bash

# run scripts suppress stderr so use stdout instead
exec 2>&1

function log {
    echo "$(date "+%Y/%m/%d %H:%M:%S") inetd: $1" >&2
}

log "Starting daemon"

exec inetd -f -e -q 1 -R 60
