#!/bin/bash

cd `dirname $0`

for path in `find ../.. -type d -name .bzr -depth 2`; do
    path=`dirname $path`
    CHECKPROJPATHS+=("$path")
done

for projpath in "${CHECKPROJPATHS[@]}"; do
    projname=`basename $projpath`
    echo "Tagging $projname with '$1' ..."
    bash -c "cd \"$projpath\"; bzr tag $1" 
    echo "======================="
done

exit 0
