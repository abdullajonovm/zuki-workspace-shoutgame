#!/bin/bash

# check scripts suppress stdout so use stderr instead
exec 1>&2

function log {
    echo "$(date "+%Y/%m/%d %H:%M:%S") consul-template check: $1" >&2
}

if [ ! "$CONSUL_TEMPLATE_USER" ]; then
    log "Missing CONSUL_TEMPLATE_USER. Exiting."
    exit 1
fi

CONSUL_TEMPLATE_COMMANDS=$(ps -e -o args | grep "^consul-template ")

if [ ! "$CONSUL_TEMPLATE_COMMANDS" ]; then
    log "No consul-template process found - exiting"
    exit 1
fi

if grep -q " -once" <<<"$CONSUL_TEMPLATE_COMMANDS"; then
    log "Bootstrap instance of consul-template process found - exiting"
    exit 1
fi

log "Consul-template appears to be running"
exit 0
