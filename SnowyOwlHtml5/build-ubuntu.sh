#!/bin/bash

cd "$(dirname "${BASH_SOURCE[0]}")"

boldesc="$(tput bold)"
exitesc="$(tput sgr0)"

if ! dpkg -l nodejs >/dev/null 2>&1; then
    echo -e "\n${boldesc}Installing Node.js${exitesc}\n" >&2
    NODE_VERSION=8
    UBUNTU_DIST="$(lsb_release -c -s)"
    curl -s https://deb.nodesource.com/gpgkey/nodesource.gpg.key | apt-key add -
    echo "deb https://deb.nodesource.com/node_$NODE_VERSION.x $UBUNTU_DIST main" > /etc/apt/sources.list.d/nodesource.list
    echo "deb-src https://deb.nodesource.com/node_$NODE_VERSION.x $UBUNTU_DIST main" >> /etc/apt/sources.list.d/nodesource.list
    apt-get update
    if ! apt-get install -y nodejs; then
        echo -e "\nError during Node.js installation - aborting build" >&2
        exit 1
    fi
fi

if ! npm ls -g yarn >/dev/null 2>&1; then
    echo -e "\n${boldesc}Installing yarn${exitesc}\n" >&2
    if ! npm install -g yarn; then
        echo -e "\nError during yarn installation - aborting build" >&2
        exit 1
    fi
fi

echo -e "\n${boldesc}Installing library dependencies${exitesc}\n"
if ! yarn; then
    echo -e "\nYarn returned error - aborting build" >&2
    exit 1
fi

echo -e "\n${boldesc}Building project${exitesc}"
if ! npm run build; then
    echo -e "\nError during build" >&2
    exit 1
fi

