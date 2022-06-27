#!/bin/bash

# run scripts suppress stderr so use stdout instead
exec 2>&1

function log {
    echo "$(date "+%Y/%m/%d %H:%M:%S") consul-template: $1" >&2
}

log "Starting daemon"

if [ ! "$CONSUL_TEMPLATE_USER" ]; then
    log "Missing CONSUL_TEMPLATE_USER. Exiting."
    exit 1
fi
if [ ! "$CONSUL_TEMPLATES" ]; then
    log "Missing CONSUL_TEMPLATES. Exiting."
    exit 1
fi

if grep -q docker /proc/1/cgroup; then
    # We're in a docker container
    HOST_IP=$(ip route | awk '/^default/ {print $3}')
    CONSUL_ADDR_ARG="-consul-addr=$HOST_IP:8500"
fi

CONSUL_TEMPLATE_ARGS=()
for pair_text in $CONSUL_TEMPLATES; do
    CONSUL_TEMPLATE_ARGS+=("-template" "$pair_text")
done

function template_fail {
    [ "$1" ] && log "$1"
    CHILD_JOB_PIDS=$(jobs -lp)
    kill -9 $CHILD_JOB_PIDS 
    wait $CHILD_JOB_PIDS 2>&-
    killall -9 consul-template 
    rm -rf "$JOB_DIR" >&-
    exit 2
}
trap "template_fail" TERM

if [ -f "$ENV_FILE" ]; then
    while read env_file_line; do
        if [[ ! "$env_file_line" =~ ^[[:space:]]*# ]]; then
            eval "export $env_file_line"
        fi
    done <"$ENV_FILE"
fi

# Render templates once before considering this service "up"
JOB_DIR=$(mktemp -d)
{ chpst -u "$CONSUL_TEMPLATE_USER" \
     consul-template -once -log-level=debug \
         $CONSUL_ADDR_ARG \
         "${CONSUL_TEMPLATE_ARGS[@]}" \
  ; echo "$?" >"${JOB_DIR}/1"; } &

sleep 10 &

wait -n

if [ ! -e "$JOB_DIR/1" ]; then
    template_fail "Templates failed to render on start up. Exiting. Consul may be missing a value required by a template."
elif [ "$(<"$JOB_DIR/1")" != "0" ]; then
    template_fail "Templates rendered on start up with error. Exiting."
fi

exec chpst -u "$CONSUL_TEMPLATE_USER" \
     consul-template -log-level=info \
         $CONSUL_ADDR_ARG \
         "${CONSUL_TEMPLATE_ARGS[@]}"
