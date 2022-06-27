#!/bin/bash

function log {
    echo "$(date "+%Y/%m/%d %H:%M:%S") wds: $1"
}

log "Starting daemon"

set -e

cd /etc/nginx

HOST_DIR="/mnt/host/$NOMAD_ALLOC_INDEX/sites-available"
mkdir -p "$HOST_DIR"
chown -R $NGINX_USER:$NGINX_GROUP "$HOST_DIR"
chmod 755 "$HOST_DIR"
rm -rf /etc/nginx/sites-available
ln -sf "$HOST_DIR"

cd /var

HOST_DIR="/mnt/host/$NOMAD_ALLOC_INDEX/www"
mkdir -p "$HOST_DIR"
chown -R $NGINX_USER:$NGINX_GROUP "$HOST_DIR"
chmod 755 "$HOST_DIR"
ln -sf "$HOST_DIR"

HOST_DIR="/mnt/host/$NOMAD_ALLOC_INDEX/upload"
mkdir -p "$HOST_DIR"
chown -R $NGINX_USER:$NGINX_GROUP "$HOST_DIR"
chmod 755 "$HOST_DIR"
ln -sf "$HOST_DIR"

cd /var/log

HOST_DIR="/mnt/host/$NOMAD_ALLOC_INDEX/log"
mkdir -p "$HOST_DIR"
chown -R $NGINX_USER:$NGINX_GROUP "$HOST_DIR"
chmod 755 "$HOST_DIR"
rm -rf /var/log/nginx
ln -sf "$HOST_DIR" nginx

SAVE_CERTS_OUTPUT=("$(/usr/local/sbin/save-certs.sh)")
readarray -t SSL_TEMPLATE_ARGS <<<"$SAVE_CERTS_OUTPUT"

/bin/bash /usr/local/sbin/dclone.sh

set +e

NAMESERVER_IP="$(awk '/^nameserver\>/ {print $2}' /etc/resolv.conf)"
if [ $? -ne 0 ]; then
    log "Error detecting nameserver"
    exit 1
fi

sed "s/\${NAMESERVER_IP}/$NAMESERVER_IP/g" /etc/nginx/letsencrypt.tmpl >/etc/nginx/letsencrypt
if [ $? -ne 0 ]; then
    log "Error configuring letsencrypt proxy"
    exit 1
fi

# read returns exit status of 1 if it is terminated by the input stream
read -rd '' CONSUL_TEMPLATE_CMD <<EOF || true
/usr/local/sbin/consul-template.sh \
    -log-level=debug \
    ${SSL_TEMPLATE_ARGS[@]} \
    $@ \
    -exec-reload-signal=SIGHUP \
    -exec "nginx -c /etc/nginx/nginx.conf -g 'daemon off;'"
EOF

exec /usr/local/sbin/pre-launch.sh \
    -D "$NGINX_HOME" \
    -c "$CONSUL_TEMPLATE_CMD"
