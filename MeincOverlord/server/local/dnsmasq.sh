#!/bin/bash

cd "$(dirname "${BASH_SOURCE[0]}")"

DEFAULT_NETWORK_INTERFACE="$(netstat -nr | awk '$1 ~ /^(default|0\.0\.0\.0)$/ {print $NF}' | head -n 1)"
# Obtain DNS server from DHCP subsystem in case the user has configured the
# local workstation to use the dnsmasq started below as the primary DNS server
DNS_SERVER="$(ipconfig getpacket "$DEFAULT_NETWORK_INTERFACE" | awk '/^domain_name_server[[:space:]]/ {gsub(/[{}]/, "", $NF); print $NF}')"

set -x

# On OS X --hostsdir isn't yet supported so we use --addn-hosts instead. A side effect is that
# changes in the hosts file will only be detected upon restarting dnsmasq.
sudo dnsmasq --no-resolv --server="$DNS_SERVER" --addn-hosts=../mnt/dnsmasq-hostsdir/hosts --no-daemon --log-queries 2>&1 | grep 'shoutgameplay\.com'
