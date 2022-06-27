#!/bin/bash

function log {
    echo "$(date "+%Y/%m/%d %H:%M:%S") mrsoa: $1"
}

log "Starting daemon"

set -e

cd "$MRSOA_HOME"

DIRS_TO_LINK="conf deploy webapps osgi logs"
for DIR_TO_LINK in $DIRS_TO_LINK; do
    HOST_DIR="/mnt/host/$NOMAD_ALLOC_INDEX/$DIR_TO_LINK"
    mkdir -p "$HOST_DIR"
    chown -R "$MRSOA_USER:$MRSOA_GROUP" "$HOST_DIR"
    chmod 750 "$HOST_DIR"
    if readlink "$DIR_TO_LINK" >/dev/null; then
        rm "$DIR_TO_LINK"
    else
        rmdir "$DIR_TO_LINK"
    fi
    ln -sf "$HOST_DIR"
done

/bin/bash /usr/local/sbin/dclone.sh

# read returns exit status of 1 if it is terminated by the input stream
read -rd '' CONSUL_TEMPLATE_CMD <<EOF || true
/usr/local/sbin/consul-template.sh \
    -log-level=trace \
    -template "$MRSOA_HOME/conf/meinc.properties.ctmpl:$MRSOA_HOME/conf/meinc.properties" \
    -template "$MEINC_HOME/keys/cluster_domain_ssl_cert.jks.ctmpl:$MEINC_HOME/keys/cluster_domain_ssl_cert.jks" \
    $@ \
    -exec-reload-signal=SIGHUP \
    -exec "$MRSOA_HOME/bin/mrsoa.sh start"
EOF

export MRSOA_DAEMON=no

exec /usr/local/sbin/pre-launch.sh \
    -D "$MRSOA_HOME" \
    -u "$MRSOA_USER" \
    -c "$CONSUL_TEMPLATE_CMD"
