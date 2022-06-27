#!/bin/bash

if grep -q docker /proc/1/cgroup; then
    # We're in a docker container
    HOST_IP=$(ip route | awk '/^default/ {print $3}')
    CONSUL_ADDR_ARG="-consul-addr=$HOST_IP:8500"
fi

# consul-template logs to stderr which inconveniently necessitates the -stderr flag when viewing
# nomad logs, so send stderr logging to stdout for convenience
exec consul-template $CONSUL_ADDR_ARG "$@" 2>&1
