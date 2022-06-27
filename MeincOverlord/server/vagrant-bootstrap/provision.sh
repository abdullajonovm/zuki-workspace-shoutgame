#!/bin/bash

set -e

cd "$(dirname "${BASH_SOURCE[0]}")"

echo "Installing packages..."

# Update apt and get dependencies
DEBIAN_FRONTEND=noninteractive \
    apt-get update
DEBIAN_FRONTEND=noninteractive \
    apt-get install -y vim unzip tree python python3 python3-pip jq

pip3 install pyyaml

echo "Configuring networks..."

function ip2int {
    local a b c d
    IFS=. read a b c d <<<"$1"
    echo $(((((((a << 8) | b) << 8) | c) << 8) | d))
}

function int2ip {
    local ip_int=$1
    local _ ip
    for _ in $(seq 4); do
        ip=$((ip_int & 0xff))${ip:+.}$ip
        ip_int=$((ip_int >> 8))
    done
    echo $ip
}

CLUSTER_ENV_PATH="/etc/cluster_env"
if [ -f "$CLUSTER_ENV_PATH" ]; then
    source "$CLUSTER_ENV_PATH"
else
    touch "$CLUSTER_ENV_PATH"
    chmod 644 "$CLUSTER_ENV_PATH"
fi

if [ "$CLUSTER_HOST_PUBLIC_IP" ]; then
    sed -i'' '/^CLUSTER_HOST_PUBLIC_IP\>/ d' "$CLUSTER_ENV_PATH"
fi

if [ ! "$CLUSTER_HOST_PUBLIC_IPv4" ]; then
    {
        FQDN=$(hostname -f)
        set +e
        FQDN_IPv4=$(host -4 $FQDN)
        if [ $? -eq 1 ]; then
            echo "No DNS record found for $FQDN and no IP address set via env var CLUSTER_HOST_PUBLIC_IPv4" >&2
            exit 1
        fi
    }
    CLUSTER_HOST_PUBLIC_IPv4=$(tail -n 1 <<<"$FQDN_IPv4" | awk '{print $NF}')
    echo "*** Auto detected public IPv4 of $CLUSTER_HOST_PUBLIC_IPv4"
    echo "CLUSTER_HOST_PUBLIC_IPv4=$CLUSTER_HOST_PUBLIC_IPv4" >>"$CLUSTER_ENV_PATH"
fi

if [ ! "$CLUSTER_HOST_PUBLIC_IPv6" ]; then
    {
        FQDN=$(hostname -f)
        set +e
        FQDN_IPv6=$(host -6 $FQDN)
        if [ $? -eq 0 -a $FQDN_IPv6 != $CLUSTER_HOST_PUBLIC_IPv4 ]; then
            CLUSTER_HOST_PUBLIC_IPv6=$(tail -n 1 <<<"$FQDN_IPv6" | awk '{print $NF}')
            echo "*** Auto detected public IPv6 of $CLUSTER_HOST_PUBLIC_IPv6"
            echo "CLUSTER_HOST_PUBLIC_IPv6=$CLUSTER_HOST_PUBLIC_IPv6" >>"$CLUSTER_ENV_PATH"
        fi
    }
fi

if [ ! "$CLUSTER_HOST_COUNT" ]; then
    echo "Missing required environment variable: CLUSTER_HOST_COUNT" >&2
    exit 1
fi

if [ "$CLUSTER_PRIVATE_NETWORK" ]; then
    # For now network id is ignored and assumed to be 24 below
    IFS=/ read -r CLUSTER_PRIVATE_NETWORK_PREFIX CLUSTER_PRIVATE_NETWORK_ID <<<"$CLUSTER_PRIVATE_NETWORK"
    CLUSTER_HOST_PRIVATE_IP=$(ip route | awk "/^${CLUSTER_PRIVATE_NETWORK//\//\\/}/ {print \$NF}")
    CLUSTER_HOST_NETWORK_INTERFACE=$(ip route | awk "/^${CLUSTER_PRIVATE_NETWORK//\//\\/}/ {print \$3}")
else
    # For now assume the default network *is* the private network
    CLUSTER_HOST_NETWORK_INTERFACE="$(ip route | awk "/^default\>/ {print \$NF}")"
    CLUSTER_HOST_PRIVATE_IP="$(ip route | awk "/\<dev $CLUSTER_HOST_NETWORK_INTERFACE\>.*\<src\>/ {print \$NF}")"
    echo "*** Auto detected private IP of $CLUSTER_HOST_PRIVATE_IP"
fi
sed -i'' '/^CLUSTER_HOST_PRIVATE_IP\>.*$/ d' "$CLUSTER_ENV_PATH"
echo "CLUSTER_HOST_PRIVATE_IP=$CLUSTER_HOST_PRIVATE_IP" >>"$CLUSTER_ENV_PATH"

if [ "$CONSUL_JOIN_STRING" ]; then
    CONSUL_JOIN_STRING="\"$CONSUL_JOIN_STRING\""
else
    if [ ! "$CLUSTER_HOST_COUNT" ]; then
        echo "At least one environment variable required: CONSUL_JOIN_STRING or CLUSTER_HOST_COUNT" >&2
        exit 1
    fi
    for i in $(seq $CLUSTER_HOST_COUNT); do
        SERF_JOIN_ADDRESS=$(( $(ip2int $CLUSTER_PRIVATE_NETWORK_PREFIX) + $i ))
        SERF_JOIN_ADDRESS="$(int2ip $SERF_JOIN_ADDRESS)"
        if [ "$SERF_JOIN_ADDRESS" != "$CLUSTER_HOST_PRIVATE_IP" ]; then
            CONSUL_JOIN_STRING="${CONSUL_JOIN_STRING:+"$CONSUL_JOIN_STRING, "}\"$SERF_JOIN_ADDRESS\""
        fi
    done
fi

echo "Configuring Docker..."

systemctl stop docker
cat >/etc/docker/daemon.json <<EOF
{
  "storage-driver": "overlay2"
}
EOF

cp docker-backup-images.sh docker-restore-images.sh /usr/local/bin/
chmod +x /usr/local/bin/*.sh

systemctl start docker

echo "Installing Consul..."

CONSUL_VERSION="0.9.2-1"
CONSUL_PACKAGE="consul_${CONSUL_VERSION}_amd64.deb"

#curl -sSL https://releases.hashicorp.com/consul/${CONSUL_VERSION}/consul_${CONSUL_VERSION}_linux_amd64.zip -o consul.zip
#curl -LO $CURL_CACHE_ARG "https://s3.amazonaws.com/nomad-static.shout.tv/$CONSUL_PACKAGE"
wget -N --no-verbose "https://s3.amazonaws.com/nomad-static.shout.tv/$CONSUL_PACKAGE" 2>&1

dpkg -i "$CONSUL_PACKAGE"

systemctl stop consul

cp consul.json /etc/consul.d/
sed -i'' "s/\\\${CLUSTER_HOST_COUNT}/$CLUSTER_HOST_COUNT/g"            /etc/consul.d/*.json
sed -i'' "s/\\\${CONSUL_JOIN_STRING}/$CONSUL_JOIN_STRING/g"            /etc/consul.d/*.json
sed -i'' "s/\\\${CLUSTER_HOST_PRIVATE_IP}/$CLUSTER_HOST_PRIVATE_IP/g"  /etc/consul.d/*.json

systemctl enable consul 2>&1
systemctl start consul

echo "Installing Consul-Template..."

CONSUL_TEMPLATE_VERSION="0.18.5"
CONSUL_TEMPLATE_PACKAGE="consul-template_${CONSUL_TEMPLATE_VERSION}_linux_amd64.tgz"

wget -N --no-verbose "https://releases.hashicorp.com/consul-template/0.18.5/$CONSUL_TEMPLATE_PACKAGE" 2>&1 \
    && tar -xzf "$CONSUL_TEMPLATE_PACKAGE" \
    && mv consul-template /usr/bin/

echo "Installing Nomad..."

NOMAD_VERSION=0.5.6-1
NOMAD_PACKAGE="nomad_${NOMAD_VERSION}_amd64.deb"

#curl -sSL https://releases.hashicorp.com/nomad/${NOMAD_VERSION}/nomad_${NOMAD_VERSION}_linux_amd64.zip -o nomad.zip
#curl -LO $CURL_CACHE_ARG "https://s3.amazonaws.com/nomad-static.shout.tv/$NOMAD_PACKAGE"
wget -N --no-verbose "https://s3.amazonaws.com/nomad-static.shout.tv/$NOMAD_PACKAGE" 2>&1

dpkg -i "$NOMAD_PACKAGE"

systemctl stop nomad

cp nomad.json /etc/nomad.d/
[ "$NOMAD_CLIENT_CPU_MHZ" ] && \
    NOMAD_CLIENT_CPU_MHZ="\"cpu_total_compute\": $NOMAD_CLIENT_CPU_MHZ,"
sed -i'' "s/\\\${NOMAD_CLIENT_CPU_MHZ}/$NOMAD_CLIENT_CPU_MHZ/g"                     /etc/nomad.d/*.json
sed -i'' "s/\\\${CLUSTER_HOST_COUNT}/$CLUSTER_HOST_COUNT/g"                         /etc/nomad.d/*.json
sed -i'' "s/\\\${CLUSTER_HOST_NETWORK_INTERFACE}/$CLUSTER_HOST_NETWORK_INTERFACE/g" /etc/nomad.d/*.json
sed -i'' "s/\\\${CLUSTER_HOST_PRIVATE_IP}/$CLUSTER_HOST_PRIVATE_IP/g"               /etc/nomad.d/*.json

systemctl enable nomad 2>&1
systemctl start nomad

echo "Installing Docker Compose..."

COMPOSE_EXE="docker-compose-$(uname -s)-$(uname -m)"
COMPOSE_EXE=${COMPOSE_EXE,,}
COMPOSE_VERSION="$(curl -s https://api.github.com/repos/docker/compose/releases/latest | grep 'tag_name' | cut -d\" -f4)"

wget -N --no-verbose "https://github.com/docker/compose/releases/download/$COMPOSE_VERSION/$COMPOSE_EXE" 2>&1
cp "$COMPOSE_EXE" /usr/bin/docker-compose
chmod +x /usr/bin/docker-compose

echo "Installing Nomad Port Connector..."

pip3 install python-iptables

NOMAD_PORT_CONNECTOR_VERSION="0.4"
NOMAD_PORT_CONNECTOR_PACKAGE="nomad-port-connector_${NOMAD_PORT_CONNECTOR_VERSION}_amd64.deb"

wget -N --no-verbose "https://s3.amazonaws.com/nomad-static.shout.tv/$NOMAD_PORT_CONNECTOR_PACKAGE" 2>&1 \
    && dpkg -i "$NOMAD_PORT_CONNECTOR_PACKAGE"

echo "Installing Nomad DNS Server..."

NOMAD_DNS_VERSION="0.5"
NOMAD_DNS_PACKAGE="nomad-dnsmasq_${NOMAD_DNS_VERSION}_amd64.deb"

wget -N --no-verbose "https://s3.amazonaws.com/nomad-static.shout.tv/$NOMAD_DNS_PACKAGE" 2>&1 \
    && dpkg -i "$NOMAD_DNS_PACKAGE"

echo "Configuring Bash Aliases..."

for f in ~/.bashrc /root/.bashrc; do
    if ! grep -q '^alias sctl=' "$f"; then
        echo "alias sctl='systemctl'"  >>"$f"
        echo "alias jctl='journalctl'" >>"$f"
        echo "alias n='nomad'"         >>"$f"
        echo "alias d='dirs -v'"       >>"$f"
        echo "alias p='pushd'"         >>"$f"
    fi
done
