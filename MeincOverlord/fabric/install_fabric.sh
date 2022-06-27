#!/bin/bash

if [ "$(id -u)" != "0" ]; then
    echo "Must run as root (sudo)"
    exit 1
fi

#PYVER=`python --version 2>&1 | cut -d" " -f2 | head -c3`
#if [ "$PYVER" != "2.7" ]; then
#    echo "Must have Python 2.7"
#    exit 1
#fi

if [ "$(which pip)" = "" ]; then
    set -ex
    curl http://python-distribute.org/distribute_setup.py | python
    curl https://raw.github.com/pypa/pip/master/contrib/get-pip.py | python
fi

set -ex
ARCHFLAGS='-arch i386 -arch x86_64' pip install --upgrade Fabric==1.10.1
