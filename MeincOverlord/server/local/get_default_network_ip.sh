#!/bin/bash

if [ "$1" = "-S" ]; then
    STRIP_SCOPE=1
    shift
fi

kernel="$(uname -s)"

case "$kernel" in
    Darwin*)
        default_ipv4_network_interface=$(netstat -nr -f inet | awk '$1 ~ /^(default|0\.0\.0\.0)$/ {print $NF}' | head -n 1)
        default_ipv6_network_interface=$(netstat -nr -f inet6 | awk '$1 ~ /^(default|::)$/ {print $NF}' | head -n 1)
        ;;
    Linux*)
        default_ipv4_network_interface=$(netstat -nr -4 | awk '$1 ~ /^(default|0\.0\.0\.0)$/ {print $NF}' | head -n 1)
        default_ipv6_network_interface=$(netstat -nr -6 | awk '$1 ~ /^(default|::)$/ {print $NF}' | head -n 1)
        ;;
esac

default_network_ipv4=$(ifconfig $default_ipv4_network_interface | awk '$1 == "inet" {print $2}' | head -n 1)
default_network_ipv4="${default_network_ipv4#addr:}"
echo "$default_network_ipv4"

if [ "$default_ipv6_network_interface" ]; then
    case "$kernel" in
        Darwin*)
            default_network_ipv6=$(ifconfig $default_ipv6_network_interface | awk '$1 == "inet6" {print $2}' | head -n 1)
            ;;
        Linux*)
            default_network_ipv6=$(ifconfig $default_ipv6_network_interface | awk '$1 == "inet6" {print $3}' | head -n 1)
            ;;
    esac
    default_network_ipv6="${default_network_ipv6#addr:}"
    if [ $STRIP_SCOPE ]; then
        default_network_ipv6="${default_network_ipv6%\%*}"
    fi
fi

echo "$default_network_ipv6"
