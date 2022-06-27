#!/bin/bash

# This script installs a standalone version of Bazaar and PyQt 4 sufficient for running the QBzr
# tools plugin

CONDA_HOME="$HOME/miniconda2"
CONDA_BIN="$CONDA_HOME/bin"
CONDA_EXE="$CONDA_BIN/conda"

if ! test -x "$CONDA_EXE"; then
    echo "Miniconda not found. Please install and retry." >&2
    echo "https://conda.io/miniconda.html" >&2
    exit 1
fi

if conda info --envs | grep -q "^bzr\>"; then
    read -rp "Conda 'bzr' environment already exists. Replace it (y/n)? " USER_INPUT
    if [ "$USER_INPUT" != "y" ]; then
        echo "Aborting" >&2
        exit 1
    fi
    echo -e "\nRemoving Conda 'bzr' environment..."
    conda env remove -y -n bzr
fi

echo -e "\nCreating Conda 'bzr' environment...\n"
if ! conda create -y -n bzr python=2.7 pip=9 pyqt=4; then
    echo "Aborting" >&2
    exit 1
fi

echo -e "\nActivating Conda 'bzr' environment..."
source "$CONDA_BIN/activate" bzr

if [ "$CONDA_DEFAULT_ENV" != bzr ]; then
    echo "Activating failed" >&2
    echo "Aborting" >&2
    exit 1
fi

echo -e "\nInstall bzr tool into Conda 'bzr' environment...\n"
if ! pip install bzr; then
    echo "Install failed" >&2
    echo "Aborting" >&2
    exit 1
fi

cat - <<EOF

Success!

In order to use this new installation of bzr you must first activate the Conda bzr environment with:

$ source activate bzr

Or if Conda isn't installed on your PATH:

$ source $CONDA_BIN/activate bzr

To deactivate the environment:

$ source deactivate

Or:

$ source $CONDA_BIN/deactivate

EOF
