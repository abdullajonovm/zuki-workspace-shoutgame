#!/bin/bash

SCRIPT_DIR="$(dirname "${BASH_SOURCE[0]}")"

cd "$SCRIPT_DIR/../"

SCM_HOST=scm.shoutgameplay.com

[ "$1" ] && VM_NAME="$1"

MAIN_DOMAIN=dev1.shoutgameplay.com

echo "Downloading latest dev certificates and keys"

CERT_DATA="$(ssh $SCM_HOST "cat /usr/local/share/dev-certs/$MAIN_DOMAIN.crt")"
if [ ! "$CERT_DATA" ]; then
    echo "Failed to retrieve certificate data" >&2
    exit 1
fi

KEY_DATA="$(ssh $SCM_HOST "cat /usr/local/share/dev-certs/$MAIN_DOMAIN.pem")"
if [ ! "$KEY_DATA" ]; then
    echo "Failed to retrieve key data" >&2
    exit 1
fi

JKS_DATA="$(ssh $SCM_HOST "base64 -w0 /usr/local/share/dev-certs/$MAIN_DOMAIN.jks")"
if [ ! "$JKS_DATA" ]; then
    echo "Failed to retrieve Java keystore data" >&2
    exit 1
fi

echo "Copying certificates and keys into VM"
set -e

vagrant ssh $VM_NAME -- "sudo mkdir -p /usr/local/share/dev-certs"
vagrant ssh $VM_NAME -- "sudo tee /usr/local/share/dev-certs/$MAIN_DOMAIN.crt >/dev/null" <<<"$CERT_DATA"
vagrant ssh $VM_NAME -- "sudo tee /usr/local/share/dev-certs/$MAIN_DOMAIN.pem >/dev/null" <<<"$KEY_DATA"
vagrant ssh $VM_NAME -- "sudo bash -c 'base64 -d - >/usr/local/share/dev-certs/$MAIN_DOMAIN.jks'" <<<"$JKS_DATA"

echo "Updating Consul in VM"

vagrant ssh $VM_NAME -- "sudo consul kv put ssl/cert/+.$MAIN_DOMAIN/pem/cert/value @/usr/local/share/dev-certs/$MAIN_DOMAIN.crt"
vagrant ssh $VM_NAME -- "sudo consul kv put ssl/cert/+.$MAIN_DOMAIN/pem/key/value @/usr/local/share/dev-certs/$MAIN_DOMAIN.pem"
vagrant ssh $VM_NAME -- "sudo base64 -w0 /usr/local/share/dev-certs/$MAIN_DOMAIN.jks | consul kv put ssl/cert/+.$MAIN_DOMAIN/jks/value -"
