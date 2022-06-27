#!/bin/bash

SCRIPT_DIR="$(dirname "${BASH_SOURCE[0]}")"

HOST_DEFAULT_IP="$("$SCRIPT_DIR/get_default_network_ip.sh" -S)"
HOST_DEFAULT_IPv4="$(head -n 1 <<<"$HOST_DEFAULT_IP")"
HOST_DEFAULT_IPv6="$(tail -n 1 <<<"$HOST_DEFAULT_IP")"

if [ "$HOST_DEFAULT_IPv4" ]; then
    echo "Detected host IP of $HOST_DEFAULT_IPv4"
fi
if [ "$HOST_DEFAULT_IPv6" ]; then
    echo "Detected host IPv6 of $HOST_DEFAULT_IPv6"
fi

cd "$SCRIPT_DIR/../"

[ "$1" ] && VM_NAME="$1"

vagrant ssh $VM_NAME -- "sudo sed -i'' 's/CLUSTER_HOST_PUBLIC_IP\(v4\)\?=.*/CLUSTER_HOST_PUBLIC_IPv4=$HOST_DEFAULT_IPv4/' /etc/cluster_env"
vagrant ssh $VM_NAME -- "sudo sed -i'' 's/CLUSTER_HOST_PUBLIC_IPv6=.*/CLUSTER_HOST_PUBLIC_IPv6=$HOST_DEFAULT_IPv6/' /etc/cluster_env"
vagrant ssh $VM_NAME -- "sudo systemctl restart nomad-dnsmasq"
vagrant ssh $VM_NAME -- "sudo systemctl restart nomad-port-connector"
