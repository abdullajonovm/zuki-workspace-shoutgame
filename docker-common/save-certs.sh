#!/bin/bash

if grep -q docker /proc/1/cgroup; then
    # We're in a docker container
    HOST_IP=$(ip route | awk '/^default/ {print $3}')
    CONSUL_ADDR_ARG="-consul-addr=$HOST_IP:8500"
fi

# Write the cluster/domain cert/key with name cluster_domain

CLUSTER_KEY_NAME="$(consul-cli.sh kv get cluster/domain/ssl/cert)"
if [ ! "$CLUSTER_KEY_NAME" ]; then
    echo "Missing cluster domain SSL entry in Consul" >&2
    exit 1
fi

SSL_PATH_PREFIX="/etc/ssl"

SSL_PATH="$SSL_PATH_PREFIX/certs/cluster_domain_ssl_cert.crt"
touch "$SSL_PATH" && chmod 444 "$SSL_PATH"
touch "$SSL_PATH.ctmpl" && chmod 444 "$SSL_PATH.ctmpl"
echo -n "{{key \"ssl/cert/$CLUSTER_KEY_NAME/pem/cert/value\"}}" >"$SSL_PATH.ctmpl"
echo -e "-template\n$SSL_PATH.ctmpl:$SSL_PATH"

SSL_PATH="$SSL_PATH_PREFIX/private/cluster_domain_ssl_key.pem"
touch "$SSL_PATH" && chmod 440 "$SSL_PATH"
touch "$SSL_PATH.ctmpl" && chmod 440 "$SSL_PATH.ctmpl"
echo -n "{{key \"ssl/cert/$CLUSTER_KEY_NAME/pem/key/value\"}}" >"$SSL_PATH.ctmpl"
echo -e "-template\n$SSL_PATH.ctmpl:$SSL_PATH"

# Write all the other ssl certs and keys

readarray -t CONSUL_CERTS <<<"$(consul-cli.sh kv get -keys ssl/cert)"
for consul_cert in "${CONSUL_CERTS[@]}"; do
    #IFS=/ read -r key_prefix key_name key_type <<<"$consul_key"
    IFS=/ read -r key_prefix1 key_prefix2 ssl_name ssl_type <<<"$consul_cert"
    if [ "$ssl_type" == "pem/cert/value" ]; then
        SSL_PATH="$SSL_PATH_PREFIX/certs/$ssl_name.crt"
        touch "$SSL_PATH" && chmod 444 "$SSL_PATH"
        touch "$SSL_PATH.ctmpl" && chmod 444 "$SSL_PATH.ctmpl"
    elif [ "$ssl_type" == "pem/key/value" ]; then
        SSL_PATH="$SSL_PATH_PREFIX/private/$ssl_name.pem"
        touch "$SSL_PATH" && chmod 440 "$SSL_PATH"
        touch "$SSL_PATH.ctmpl" && chmod 440 "$SSL_PATH.ctmpl"
    elif [ "$ssl_type" == "jks/value" ]; then
        SSL_PATH="$SSL_PATH_PREFIX/certs/$ssl_name.jks"
        touch "$SSL_PATH" && chmod 444 "$SSL_PATH"
        touch "$SSL_PATH.ctmpl" && chmod 444 "$SSL_PATH.ctmpl"
    elif [ "$ssl_type" == "jks/pass" ]; then
        SSL_PATH=
    else
        echo "Unknown ssl type $ssl_type found in consul in $consul_cert - aborting" >&2
        exit 1
    fi
    if [ "$SSL_PATH" ]; then
        echo -n "{{key \"$consul_cert\"}}" >"$SSL_PATH.ctmpl"
        echo -e "-template\n$SSL_PATH.ctmpl:$SSL_PATH"
    fi
done
